package vn.flora.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuoteService {
    public record Item(UUID productId, int quantity) {}
    public record Request(List<Item> items, String recipientName, String recipientPhone, String address,
                          LocalDate deliveryDate, String timeSlot, String cardMessage, String voucherCode) {
        public Request(List<Item> items, String recipientName, String recipientPhone, String address,
                       LocalDate deliveryDate, String timeSlot, String cardMessage) {
            this(items, recipientName, recipientPhone, address, deliveryDate, timeSlot, cardMessage, null);
        }
    }
    public record Line(UUID productId, String sku, String name, String image, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Quote(UUID id, Instant expiresAt, List<Line> items, String recipientName, String recipientPhone,
                        String address, LocalDate deliveryDate, String timeSlot, String cardMessage,
                        BigDecimal subtotal, BigDecimal discountAmount, BigDecimal shippingFee, BigDecimal total,
                        String voucherCode, String currency, String shippingMode) {}

    private final JdbcTemplate db;
    private final ObjectMapper json;
    private final RestClient catalog;
    private final VoucherService voucherService;
    private final BigDecimal fee;

    QuoteService(JdbcTemplate db, ObjectMapper json,
                 @Value("${CATALOG_URL:http://localhost:8081}") String url,
                 @Value("${MOCK_SHIPPING_FEE:30000}") BigDecimal fee,
                 VoucherService voucherService) {
        this.db = db;
        this.json = json;
        this.voucherService = voucherService;
        if (fee.signum() < 0 || fee.scale() > 2) throw new IllegalArgumentException("Invalid mock shipping fee");
        this.fee = fee.setScale(2);
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        catalog = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    public Quote create(UUID user, Request request) {
        if (user == null || request == null || request.items() == null || request.items().isEmpty() || request.items().size() > 30)
            throw invalid("Giỏ phải có từ 1 đến 30 sản phẩm");
        String name = text(request.recipientName(), 1, 120),
               phone = text(request.recipientPhone(), 8, 20),
               address = text(request.address(), 5, 500);
        if (!phone.matches("[+0-9 ()-]+")) throw invalid("Số điện thoại không hợp lệ");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (request.deliveryDate() == null || request.deliveryDate().isBefore(today) || request.deliveryDate().isAfter(today.plusDays(30)))
            throw invalid("Ngày giao phải trong 30 ngày tới");
        String slot = text(request.timeSlot(), 1, 80), card = request.cardMessage() == null ? "" : text(request.cardMessage(), 0, 1000);
        Set<UUID> seen = new HashSet<>();
        List<Line> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);

        for (Item item : request.items()) {
            if (item == null || item.productId() == null || item.quantity() < 1 || item.quantity() > 99 || !seen.add(item.productId()))
                throw invalid("Sản phẩm hoặc số lượng không hợp lệ");
            JsonNode product;
            try {
                product = catalog.get().uri("/api/products/{id}", item.productId()).exchange((req, res) -> {
                    if (res.getStatusCode().value() == 404) throw invalid("Sản phẩm không còn bán");
                    if (!res.getStatusCode().is2xxSuccessful()) throw unavailable();
                    return json.readTree(res.getBody());
                });
            } catch (ResponseStatusException ex) { throw ex; }
            catch (RestClientException | java.util.concurrent.CancellationException ex) { throw unavailable(); }

            if (product == null || !product.path("price").isNumber() || !product.path("name").isTextual() || !product.path("sku").isTextual())
                throw unavailable();
            BigDecimal price = product.get("price").decimalValue().setScale(2, RoundingMode.UNNECESSARY);
            if (price.signum() < 0) throw unavailable();
            BigDecimal amount = price.multiply(BigDecimal.valueOf(item.quantity()));
            lines.add(new Line(item.productId(), product.get("sku").asText(), product.get("name").asText(),
                product.path("image").isTextual() ? product.get("image").asText() : null, price, item.quantity(), amount));
            subtotal = subtotal.add(amount);
        }

        BigDecimal discountAmount = BigDecimal.ZERO.setScale(2);
        String appliedVoucher = null;
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            var validation = voucherService.validate(request.voucherCode(), subtotal);
            if (!validation.valid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.message());
            }
            discountAmount = validation.discountAmount();
            appliedVoucher = validation.code();
        }

        BigDecimal total = subtotal.subtract(discountAmount).add(fee);
        Quote quote = new Quote(UUID.randomUUID(), Instant.now().plusSeconds(600), List.copyOf(lines), name, phone, address,
            request.deliveryDate(), slot, card, subtotal, discountAmount, fee, total, appliedVoucher, "VND", "MOCK");

        try {
            db.update("INSERT INTO \"order\".checkout_quotes(id,user_id,snapshot,expires_at) VALUES (?,?,CAST(? AS jsonb),?)",
                quote.id(), user, json.writeValueAsString(quote), java.sql.Timestamp.from(quote.expiresAt()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize quote", ex);
        }
        return quote;
    }

    public Quote get(UUID user, UUID id) {
        var snapshots = db.queryForList("SELECT snapshot::text FROM \"order\".checkout_quotes WHERE id=? AND user_id=? AND expires_at>now()", String.class, id, user);
        if (snapshots.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Báo giá không tồn tại hoặc đã hết hạn");
        try {
            return json.readValue(snapshots.get(0), Quote.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Cannot read quote", ex);
        }
    }

    public void verifyCurrent(Quote quote) {
        for (var line : quote.items()) {
            if (line.sku() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Vui lòng lấy báo giá mới");
            try {
                catalog.get().uri("/api/products/{id}", line.productId()).exchange((req, res) -> {
                    if (res.getStatusCode().value() == 404) throw invalid("Sản phẩm không còn bán");
                    if (!res.getStatusCode().is2xxSuccessful()) throw unavailable();
                    var p = json.readTree(res.getBody());
                    if (p == null || !p.path("price").isNumber()) throw unavailable();
                    if (p.get("price").decimalValue().compareTo(line.unitPrice()) != 0)
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Giá đã thay đổi, vui lòng lấy báo giá mới");
                    return null;
                });
            } catch (ResponseStatusException ex) { throw ex; }
            catch (RestClientException | java.util.concurrent.CancellationException ex) { throw unavailable(); }
        }
    }

    private static String text(String value, int min, int max) {
        if (value == null) throw invalid("Dữ liệu không được để trống");
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) throw invalid("Độ dài dữ liệu không hợp lệ");
        return trimmed;
    }

    private static ResponseStatusException invalid(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ danh mục hoa tạm thời không khả dụng");
    }
}

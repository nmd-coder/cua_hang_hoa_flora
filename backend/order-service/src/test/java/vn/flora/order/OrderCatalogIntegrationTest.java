package vn.flora.order;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Kiểm thử tích hợp – Điểm tích hợp IP-02: Order Service -> Catalog Service.
 *
 * Trước khi tạo đơn, CodCheckoutService gọi QuoteService.verifyCurrent(quote) để đối chiếu giá trong báo giá
 * với giá hiện tại ở Catalog (GET /api/products/{id}). Catalog được thay bằng STUB HTTP.
 * QuoteService được khởi tạo với JdbcTemplate = null vì verifyCurrent() không chạm tới CSDL.
 */
@DisplayName("IP-02 Order Service ↔ Catalog Service (stub HTTP)")
class OrderCatalogIntegrationTest {
    static final UUID ROSE = UUID.fromString("10000000-0000-0000-0000-000000000001");

    private HttpServer stub;
    private String baseUrl;
    private volatile int status = 200;
    private volatile String productJson = "{\"sku\":\"FL-001\",\"name\":\"Nắng dịu dàng\",\"price\":350000}";

    @BeforeEach
    void startCatalogStub() throws IOException {
        stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        stub.createContext("/api/products", ex -> {
            byte[] b = productJson.getBytes(StandardCharsets.UTF_8);
            if (status == 200) {
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, b.length);
                ex.getResponseBody().write(b);
            } else {
                ex.sendResponseHeaders(status, -1);
            }
            ex.close();
        });
        stub.start();
        baseUrl = "http://127.0.0.1:" + stub.getAddress().getPort();
    }

    @AfterEach
    void stopCatalogStub() { stub.stop(0); }

    private QuoteService service(String url) {
        return new QuoteService(null, new ObjectMapper(), url, new BigDecimal("30000"), null);
    }

    /** Báo giá đã lưu: 2 x FL-001 với đơn giá 350.000đ tại thời điểm báo giá. */
    private static QuoteService.Quote quoteAt(String unitPrice) {
        var line = new QuoteService.Line(ROSE, "FL-001", "Nắng dịu dàng", null,
            new BigDecimal(unitPrice), 2, new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(2)));
        return new QuoteService.Quote(UUID.randomUUID(), Instant.now().plusSeconds(600), List.of(line),
            "Người nhận mẫu", "0900000000", "Địa chỉ mẫu", LocalDate.now().plusDays(1), "09-12", "",
            line.lineTotal(), BigDecimal.ZERO, new BigDecimal("30000.00"),
            line.lineTotal().add(new BigDecimal("30000.00")), null, "VND", "MOCK");
    }

    @Test
    @DisplayName("IT-10 Giá ở Catalog không đổi -> báo giá hợp lệ, cho phép đặt hàng")
    void it10_priceUnchanged() {
        assertDoesNotThrow(() -> service(baseUrl).verifyCurrent(quoteAt("350000.00")));
    }

    @Test
    @DisplayName("IT-11 Giá ở Catalog đã tăng -> 409 'Giá đã thay đổi, vui lòng lấy báo giá mới'")
    void it11_priceChanged() {
        productJson = "{\"sku\":\"FL-001\",\"name\":\"Nắng dịu dàng\",\"price\":390000}";
        var ex = assertThrows(ResponseStatusException.class, () -> service(baseUrl).verifyCurrent(quoteAt("350000.00")));
        assertEquals(409, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Giá đã thay đổi"));
    }

    @Test
    @DisplayName("IT-12 Sản phẩm đã ngừng bán (Catalog 404) -> 400 'Sản phẩm không còn bán'")
    void it12_productRemoved() {
        status = 404;
        var ex = assertThrows(ResponseStatusException.class, () -> service(baseUrl).verifyCurrent(quoteAt("350000.00")));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Sản phẩm không còn bán", ex.getReason());
    }

    @Test
    @DisplayName("IT-13 Catalog không chạy -> 503 'Dịch vụ danh mục hoa tạm thời không khả dụng'")
    void it13_catalogDown() {
        var ex = assertThrows(ResponseStatusException.class,
            () -> service("http://127.0.0.1:1").verifyCurrent(quoteAt("350000.00")));
        assertEquals(503, ex.getStatusCode().value());
    }
}

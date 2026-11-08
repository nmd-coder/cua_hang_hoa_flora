package vn.flora.order;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/**
 * Kiểm thử tích hợp – Điểm tích hợp IP-01: Order Service -> Inventory Service.
 *
 * Chiến lược Sandwich, nửa trên (top-down): dùng InventoryClient THẬT của Order Service,
 * thay Inventory Service bằng STUB HTTP (com.sun.net.httpserver.HttpServer) để chủ động
 * tạo các phản hồi 204 / 409 / 401 / 500 / 503 / timeout / mất kết nối.
 *
 * Hợp đồng (contract) đang được kiểm tra:
 *   PUT    /internal/reservations/{orderId}   header X-Internal-Key, body [{"itemId":UUID,"quantity":int}]
 *   DELETE /internal/reservations/{orderId}   header X-Internal-Key
 *   204 = thành công, 409 = không đủ tồn, 401 = sai khóa nội bộ, 5xx = lỗi kho.
 *
 * Giới hạn: stub chỉ mô phỏng hợp đồng HTTP; KHÔNG kiểm tra logic khóa dòng/trừ tồn trong PostgreSQL
 * của Inventory Service (phần đó được kiểm tra ở script tích hợp thật scripts/test-integration-order-inventory.ps1).
 */
@DisplayName("IP-01 Order Service ↔ Inventory Service (stub HTTP)")
class OrderInventoryIntegrationTest {
    static final String KEY = "test-internal-key-not-for-real-use-123456"; // >= 32 ký tự như cấu hình thật
    static final UUID ROSE = UUID.fromString("10000000-0000-0000-0000-000000000001");
    static final UUID TULIP = UUID.fromString("10000000-0000-0000-0000-000000000002");

    record Captured(String method, String path, String key, String contentType, String body) {}

    private final ObjectMapper json = new ObjectMapper();
    private final List<Captured> calls = new CopyOnWriteArrayList<>();
    private HttpServer stub;
    private ExecutorService pool;
    private String baseUrl;
    private volatile int status = 204;   // mã phản hồi stub sẽ trả
    private volatile long delayMs = 0;   // độ trễ giả lập

    @BeforeEach
    void startInventoryStub() throws IOException {
        stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        stub.createContext("/internal/reservations", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String key = ex.getRequestHeaders().getFirst("X-Internal-Key");
            calls.add(new Captured(ex.getRequestMethod(), ex.getRequestURI().getPath(), key,
                ex.getRequestHeaders().getFirst("Content-Type"), body));
            if (delayMs > 0) {
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            // Giống InternalController thật: sai khóa nội bộ -> 401
            int code = KEY.equals(key) ? status : 401;
            ex.sendResponseHeaders(code, -1);
            ex.close();
        });
        pool = Executors.newCachedThreadPool();
        stub.setExecutor(pool);
        stub.start();
        baseUrl = "http://127.0.0.1:" + stub.getAddress().getPort();
    }

    @AfterEach
    void stopInventoryStub() {
        stub.stop(0);
        pool.shutdownNow();
    }

    /** Báo giá mẫu: 2 x FL-001 (350.000đ) + 1 x FL-002 (420.000đ). */
    private static QuoteService.Quote sampleQuote() {
        var l1 = new QuoteService.Line(ROSE, "FL-001", "Nắng dịu dàng", null,
            new BigDecimal("350000.00"), 2, new BigDecimal("700000.00"));
        var l2 = new QuoteService.Line(TULIP, "FL-002", "Hoa mẫu 2", null,
            new BigDecimal("420000.00"), 1, new BigDecimal("420000.00"));
        return new QuoteService.Quote(UUID.randomUUID(), Instant.now().plusSeconds(600), List.of(l1, l2),
            "Người nhận mẫu", "0900000000", "Địa chỉ mẫu", LocalDate.now().plusDays(1), "09-12", "Thiệp",
            new BigDecimal("1120000.00"), BigDecimal.ZERO, new BigDecimal("30000.00"), new BigDecimal("1150000.00"),
            null, "VND", "MOCK");
    }

    private static int statusOf(ResponseStatusException ex) { return ex.getStatusCode().value(); }

    @Test
    @DisplayName("IT-01 Giữ tồn thành công: gửi đúng method, URL, khóa nội bộ và payload")
    void it01_reserveSendsCorrectContract() throws Exception {
        var client = new InventoryClient(baseUrl, KEY);
        UUID orderId = UUID.randomUUID();

        assertDoesNotThrow(() -> client.reserve(orderId, sampleQuote()));

        assertEquals(1, calls.size(), "Chỉ được gọi Inventory đúng 1 lần");
        Captured c = calls.get(0);
        assertEquals("PUT", c.method());
        assertEquals("/internal/reservations/" + orderId, c.path());
        assertEquals(KEY, c.key());
        assertTrue(c.contentType() != null && c.contentType().startsWith("application/json"));
        JsonNode body = json.readTree(c.body());
        assertTrue(body.isArray());
        assertEquals(2, body.size());
        assertEquals(ROSE.toString(), body.get(0).get("itemId").asText());
        assertEquals(2, body.get(0).get("quantity").asInt());
        assertEquals(TULIP.toString(), body.get(1).get("itemId").asText());
        assertEquals(1, body.get(1).get("quantity").asInt());
    }

    @Test
    @DisplayName("IT-02 Không đủ tồn: Inventory trả 409 -> Order nhận 409 CONFLICT 'Không đủ tồn kho'")
    void it02_outOfStockIsMappedToConflict() {
        status = 409;
        var client = new InventoryClient(baseUrl, KEY);
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        assertEquals(409, statusOf(ex));
        assertEquals("Không đủ tồn kho", ex.getReason());
    }

    @Test
    @DisplayName("IT-03 Sai khóa nội bộ: Inventory trả 401 -> Order trả 503 (không lộ chi tiết bảo mật)")
    void it03_wrongInternalKey() {
        var client = new InventoryClient(baseUrl, "wrong-key-but-long-enough-0123456789");
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        assertEquals(503, statusOf(ex));
        assertEquals(1, calls.size());
    }

    @Test
    @DisplayName("IT-04 Inventory lỗi nội bộ 500 -> Order trả 503 và gợi ý thử lại cùng khóa")
    void it04_inventoryServerError() {
        status = 500;
        var client = new InventoryClient(baseUrl, KEY);
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        assertEquals(503, statusOf(ex));
        assertTrue(ex.getReason().contains("Kho tạm thời không khả dụng"));
    }

    @Test
    @DisplayName("IT-05 Inventory không chạy (từ chối kết nối) -> Order trả 503")
    void it05_inventoryUnreachable() {
        var client = new InventoryClient("http://127.0.0.1:1", KEY);
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        assertEquals(503, statusOf(ex));
    }

    @Test
    @DisplayName("IT-06 Inventory phản hồi chậm hơn read-timeout 5s -> Order ngắt và trả 503")
    void it06_inventoryTimeout() {
        delayMs = 6_500;
        var client = new InventoryClient(baseUrl, KEY);
        long start = System.nanoTime();
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertEquals(503, statusOf(ex));
        assertTrue(elapsedMs < 6_400, "Order phải tự ngắt trước khi Inventory trả lời, thực tế: " + elapsedMs + "ms");
    }

    @Test
    @DisplayName("IT-07 Order chưa cấu hình INTERNAL_API_KEY -> 503 và KHÔNG gửi request sang Inventory")
    void it07_missingInternalKeyConfiguration() {
        var client = new InventoryClient(baseUrl, "");
        var ex = assertThrows(ResponseStatusException.class, () -> client.reserve(UUID.randomUUID(), sampleQuote()));
        assertEquals(503, statusOf(ex));
        assertEquals("Chưa cấu hình kết nối kho", ex.getReason());
        assertTrue(calls.isEmpty(), "Không được gọi Inventory khi thiếu khóa");
    }

    @Test
    @DisplayName("IT-08 Hủy đơn - nhả tồn thành công: DELETE đúng URL và khóa nội bộ")
    void it08_releaseSendsCorrectContract() {
        var client = new InventoryClient(baseUrl, KEY);
        UUID orderId = UUID.randomUUID();
        assertDoesNotThrow(() -> client.release(orderId));
        assertEquals(1, calls.size());
        assertEquals("DELETE", calls.get(0).method());
        assertEquals("/internal/reservations/" + orderId, calls.get(0).path());
        assertEquals(KEY, calls.get(0).key());
    }

    @Test
    @DisplayName("IT-09 Hủy đơn khi Inventory đang lỗi 503 -> Order nhận 503 (đơn giữ CANCEL_PENDING để thử lại)")
    void it09_releaseWhenInventoryDown() {
        status = 503;
        var client = new InventoryClient(baseUrl, KEY);
        var ex = assertThrows(ResponseStatusException.class, () -> client.release(UUID.randomUUID()));
        assertEquals(503, statusOf(ex));
    }
}

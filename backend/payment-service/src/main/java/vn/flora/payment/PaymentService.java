package vn.flora.payment;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {
    private final JdbcTemplate db;
    private final RestClient orderClient;

    public PaymentService(JdbcTemplate db, @Value("${ORDER_URL:http://localhost:8083}") String orderUrl) {
        this.db = db;
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.orderClient = RestClient.builder().baseUrl(orderUrl).requestFactory(factory).build();
    }

    public record PaymentResult(UUID paymentId, UUID orderId, String provider, String method, BigDecimal amount,
                                String status, String payUrl, String qrCode, boolean isMock, String message) {}

    @Transactional
    public PaymentResult createPayment(UUID orderId, BigDecimal amount, String provider, String method) {
        if (orderId == null || amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin thanh toán không hợp lệ");
        }

        String prov = provider != null ? provider.trim().toUpperCase(Locale.ROOT) : "COD";
        String meth = method != null ? method.trim().toUpperCase(Locale.ROOT) : "COD";
        UUID paymentId = UUID.randomUUID();

        if ("MOMO".equals(prov)) {
            String mockTransId = "MOMO-MOCK-" + UUID.randomUUID().toString().substring(0, 8);
            db.update("""
                INSERT INTO payment.payment_transactions(id, order_id, provider, method, amount, currency, status, provider_transaction_id, created_at, updated_at)
                VALUES (?, ?, 'MOMO', 'MOMO_WALLET', ?, 'VND', 'PENDING', ?, now(), now())
                """, paymentId, orderId, amount, mockTransId);

            String payUrl = "/thanh-toan/momo-mock?paymentId=" + paymentId + "&orderId=" + orderId + "&amount=" + amount;
            String qrCode = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + payUrl;

            return new PaymentResult(paymentId, orderId, "MOMO", "MOMO_WALLET", amount, "PENDING", payUrl, qrCode, true,
                "[GIẢ LẬP MÔI TRƯỜNG SANDBOX] Vui lòng xác nhận giao dịch thử nghiệm.");
        } else {
            // Default: COD
            db.update("""
                INSERT INTO payment.payment_transactions(id, order_id, provider, method, amount, currency, status, provider_transaction_id, created_at, updated_at)
                VALUES (?, ?, 'COD', 'CASH_ON_DELIVERY', ?, 'VND', 'PENDING', NULL, now(), now())
                """, paymentId, orderId, amount);

            return new PaymentResult(paymentId, orderId, "COD", "CASH_ON_DELIVERY", amount, "PENDING", null, null, false,
                "Thanh toán tiền mặt khi nhận hàng (COD).");
        }
    }

    @Transactional
    public Map<String, Object> handleMoMoCallback(String eventId, String transId, UUID paymentId, int resultCode) {
        if (eventId == null || eventId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu mã sự kiện webhook");
        }

        // Idempotent webhook receipt
        int existing = db.queryForObject("""
            SELECT count(*) FROM payment.webhook_receipts WHERE provider = 'MOMO' AND provider_event_id = ?
            """, Integer.class, eventId);
        if (existing > 0) {
            return Map.of("status", "ALREADY_PROCESSED", "eventId", eventId);
        }

        db.update("""
            INSERT INTO payment.webhook_receipts(id, provider, provider_event_id, processing_status, received_at)
            VALUES (?, 'MOMO', ?, 'PROCESSED', now())
            """, UUID.randomUUID(), eventId);

        List<Map<String, Object>> txRows = db.queryForList("""
            SELECT id, order_id, amount, status FROM payment.payment_transactions WHERE id = ? FOR UPDATE
            """, paymentId);

        if (txRows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch thanh toán");
        }

        UUID orderId = (UUID) txRows.get(0).get("order_id");
        if (resultCode == 0) {
            db.update("""
                UPDATE payment.payment_transactions
                SET status = 'PAID', provider_transaction_id = COALESCE(provider_transaction_id, ?), updated_at = now()
                WHERE id = ?
                """, transId, paymentId);

            // Notify order service if reachable
            try {
                orderClient.post().uri("/api/internal/orders/{orderId}/payment-status", orderId)
                    .body(Map.of("status", "PAID"))
                    .retrieve().toBodilessEntity();
            } catch (Exception ex) {
                // Log and continue
            }
            return Map.of("status", "SUCCESS", "paymentId", paymentId, "orderId", orderId, "resultCode", 0);
        } else {
            db.update("UPDATE payment.payment_transactions SET status = 'FAILED', updated_at = now() WHERE id = ?", paymentId);
            return Map.of("status", "FAILED", "paymentId", paymentId, "orderId", orderId, "resultCode", resultCode);
        }
    }

    public Map<String, Object> getPaymentForOrder(UUID orderId) {
        List<Map<String, Object>> rows = db.queryForList("""
            SELECT id, order_id, provider, method, amount, currency, status, provider_transaction_id, created_at, updated_at
            FROM payment.payment_transactions
            WHERE order_id = ?
            ORDER BY created_at DESC
            LIMIT 1
            """, orderId);
        if (rows.isEmpty()) {
            return Map.of("order_id", orderId, "status", "UNPAID", "method", "COD");
        }
        return rows.get(0);
    }

    @Transactional
    public Map<String, Object> createRefund(UUID orderId, UUID paymentId, BigDecimal amount, String reason, String requestKey) {
        if (orderId == null || paymentId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || requestKey == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin yêu cầu hoàn tiền không hợp lệ");
        }

        // Idempotency check via request_key
        List<Map<String, Object>> existing = db.queryForList("""
            SELECT id, order_id, payment_id, amount, status, reason, created_at
            FROM payment.refunds WHERE request_key = ?
            """, requestKey);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        UUID refundId = UUID.randomUUID();
        db.update("""
            INSERT INTO payment.refunds(id, order_id, payment_id, amount, status, request_key, reason, created_at, updated_at)
            VALUES (?, ?, ?, ?, 'COMPLETED', ?, ?, now(), now())
            """, refundId, orderId, paymentId, amount, requestKey, reason);

        db.update("UPDATE payment.payment_transactions SET status = 'REFUNDED', updated_at = now() WHERE id = ?", paymentId);

        return Map.of(
            "refundId", refundId,
            "orderId", orderId,
            "paymentId", paymentId,
            "amount", amount,
            "status", "COMPLETED",
            "reason", reason
        );
    }

    public List<Map<String, Object>> listRefunds() {
        return db.queryForList("""
            SELECT r.id, r.order_id, r.payment_id, r.amount, r.status, r.reason, r.created_at,
                   p.provider, p.method
            FROM payment.refunds r
            JOIN payment.payment_transactions p ON p.id = r.payment_id
            ORDER BY r.created_at DESC
            LIMIT 100
            """);
    }
}


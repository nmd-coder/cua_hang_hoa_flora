package vn.flora.payment;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public record CreateRequest(UUID orderId, BigDecimal amount, String provider, String method) {}

    @PostMapping("/create")
    public ResponseEntity<PaymentService.PaymentResult> create(@RequestBody CreateRequest req) {
        return ResponseEntity.ok(paymentService.createPayment(req.orderId(), req.amount(), req.provider(), req.method()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getForOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentForOrder(orderId));
    }

    public record CallbackRequest(String eventId, String transId, UUID paymentId, Integer resultCode) {}

    @PostMapping("/momo/callback")
    public ResponseEntity<Map<String, Object>> momoCallback(@RequestBody CallbackRequest req) {
        String eventId = req.eventId() != null ? req.eventId() : "evt-" + req.transId();
        int code = req.resultCode() != null ? req.resultCode() : 0;
        return ResponseEntity.ok(paymentService.handleMoMoCallback(eventId, req.transId(), req.paymentId(), code));
    }

    @PostMapping("/momo/simulate")
    public ResponseEntity<Map<String, Object>> simulatePayment(@RequestBody Map<String, Object> body) {
        UUID paymentId = UUID.fromString((String) body.get("paymentId"));
        String transId = "MOMO-" + System.currentTimeMillis();
        String eventId = "MOMO-EVENT-" + UUID.randomUUID();
        return ResponseEntity.ok(paymentService.handleMoMoCallback(eventId, transId, paymentId, 0));
    }

    public record RefundRequest(UUID orderId, UUID paymentId, BigDecimal amount, String reason, String requestKey) {}

    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refund(@RequestBody RefundRequest req) {
        String key = req.requestKey() != null ? req.requestKey() : "ref-" + req.paymentId();
        return ResponseEntity.ok(paymentService.createRefund(req.orderId(), req.paymentId(), req.amount(), req.reason(), key));
    }

    @GetMapping("/refunds")
    public List<Map<String, Object>> listRefunds() {
        return paymentService.listRefunds();
    }
}


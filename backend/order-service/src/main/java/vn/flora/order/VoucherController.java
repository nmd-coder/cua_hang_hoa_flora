package vn.flora.order;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class VoucherController {
    private final VoucherService vouchers;
    private final IdentityClient identity;

    public VoucherController(VoucherService vouchers, IdentityClient identity) {
        this.vouchers = vouchers;
        this.identity = identity;
    }

    public record ValidateRequest(String code, BigDecimal subtotal) {}

    @PostMapping("/api/vouchers/validate")
    public ResponseEntity<VoucherService.VoucherValidation> validate(@RequestBody ValidateRequest req) {
        VoucherService.VoucherValidation result = vouchers.validate(req.code(), req.subtotal() != null ? req.subtotal() : BigDecimal.ZERO);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(result);
    }

    @GetMapping("/api/vouchers/active")
    public List<Map<String, Object>> listActive() {
        return vouchers.listActive();
    }

    // Admin endpoints
    @GetMapping("/api/admin/vouchers")
    public List<Map<String, Object>> listAll(@RequestHeader(value = "Authorization", required = false) String auth) {
        var actor = identity.requireActor(auth);
        if (!actor.roles().contains("ADMIN") && !actor.roles().contains("STAFF")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền quản lý voucher");
        }
        return vouchers.listAll();
    }

    @PostMapping("/api/admin/vouchers")
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody VoucherService.CreateVoucherRequest req) {
        var actor = identity.requireActor(auth);
        if (!actor.roles().contains("ADMIN")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ quản trị viên được tạo voucher");
        }
        UUID id = vouchers.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "SUCCESS"));
    }

    @PatchMapping("/api/admin/vouchers/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        var actor = identity.requireActor(auth);
        if (!actor.roles().contains("ADMIN")) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ quản trị viên được đổi trạng thái voucher");
        }
        vouchers.updateStatus(id, body.get("status"));
        return ResponseEntity.noContent().build();
    }
}


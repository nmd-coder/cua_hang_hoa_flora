package vn.flora.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoucherService {
    private final JdbcTemplate db;

    public VoucherService(JdbcTemplate db) {
        this.db = db;
    }

    public record VoucherValidation(boolean valid, UUID id, String code, String name, BigDecimal discountAmount, String message) {}

    public VoucherValidation validate(String rawCode, BigDecimal subtotal) {
        if (rawCode == null || rawCode.isBlank()) {
            return new VoucherValidation(false, null, "", "", BigDecimal.ZERO, "Mã voucher không được để trống");
        }
        String code = rawCode.trim().toUpperCase(Locale.ROOT);

        List<Map<String, Object>> rows = db.queryForList("""
            SELECT id, code, name, discount_type, discount_value, max_discount, min_order_value,
                   starts_at, expires_at, max_uses, used_count, status
            FROM "order".vouchers
            WHERE code = ?
            """, code);

        if (rows.isEmpty()) {
            return new VoucherValidation(false, null, code, "", BigDecimal.ZERO, "Mã voucher không tồn tại");
        }

        Map<String, Object> v = rows.get(0);
        String status = (String) v.get("status");
        if (!"ACTIVE".equals(status)) {
            return new VoucherValidation(false, null, code, "", BigDecimal.ZERO, "Voucher hiện không hoạt động");
        }

        Instant now = Instant.now();
        Instant startsAt = ((java.sql.Timestamp) v.get("starts_at")).toInstant();
        Instant expiresAt = ((java.sql.Timestamp) v.get("expires_at")).toInstant();

        if (now.isBefore(startsAt) || now.isAfter(expiresAt)) {
            return new VoucherValidation(false, null, code, "", BigDecimal.ZERO, "Voucher chưa tới hạn hoặc đã hết hạn");
        }

        int maxUses = ((Number) v.get("max_uses")).intValue();
        int usedCount = ((Number) v.get("used_count")).intValue();
        if (usedCount >= maxUses) {
            return new VoucherValidation(false, null, code, "", BigDecimal.ZERO, "Voucher đã hết lượt sử dụng");
        }

        BigDecimal minOrderValue = (BigDecimal) v.get("min_order_value");
        if (subtotal.compareTo(minOrderValue) < 0) {
            return new VoucherValidation(false, null, code, "", BigDecimal.ZERO,
                "Đơn hàng chưa đạt giá trị tối thiểu " + minOrderValue.longValue() + "đ");
        }

        UUID id = (UUID) v.get("id");
        String name = (String) v.get("name");
        String type = (String) v.get("discount_type");
        BigDecimal val = (BigDecimal) v.get("discount_value");
        BigDecimal maxDisc = (BigDecimal) v.get("max_discount");

        BigDecimal discount;
        if ("PERCENTAGE".equalsIgnoreCase(type)) {
            discount = subtotal.multiply(val).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (maxDisc != null && maxDisc.compareTo(BigDecimal.ZERO) > 0 && discount.compareTo(maxDisc) > 0) {
                discount = maxDisc;
            }
        } else {
            discount = val;
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return new VoucherValidation(true, id, code, name, discount.setScale(2, RoundingMode.HALF_UP), "Áp dụng voucher thành công");
    }

    @Transactional
    public void redeem(UUID voucherId, UUID orderId, UUID userId, BigDecimal discountApplied) {
        if (voucherId == null || orderId == null || userId == null) return;
        int updated = db.update("""
            UPDATE "order".vouchers
            SET used_count = used_count + 1
            WHERE id = ? AND used_count < max_uses AND status = 'ACTIVE'
            """, voucherId);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher đã hết lượt sử dụng trong lúc xử lý đơn");
        }
        db.update("""
            INSERT INTO "order".voucher_redemptions(id, voucher_id, order_id, user_id, discount_applied, created_at)
            VALUES (?, ?, ?, ?, ?, now())
            ON CONFLICT (voucher_id, order_id) DO NOTHING
            """, UUID.randomUUID(), voucherId, orderId, userId, discountApplied);
    }

    @Transactional
    public void release(UUID orderId) {
        if (orderId == null) return;
        List<UUID> vouchers = db.queryForList("""
            SELECT voucher_id FROM "order".voucher_redemptions WHERE order_id = ?
            """, UUID.class, orderId);
        for (UUID vId : vouchers) {
            db.update("UPDATE \"order\".vouchers SET used_count = GREATEST(0, used_count - 1) WHERE id = ?", vId);
            db.update("DELETE FROM \"order\".voucher_redemptions WHERE voucher_id = ? AND order_id = ?", vId, orderId);
        }
    }

    public List<Map<String, Object>> listActive() {
        return db.queryForList("""
            SELECT id, code, name, discount_type, discount_value, max_discount, min_order_value, starts_at, expires_at
            FROM "order".vouchers
            WHERE status = 'ACTIVE' AND expires_at > now() AND used_count < max_uses
            ORDER BY min_order_value ASC
            """);
    }

    public List<Map<String, Object>> listAll() {
        return db.queryForList("""
            SELECT id, code, name, discount_type, discount_value, max_discount, min_order_value,
                   starts_at, expires_at, max_uses, used_count, status
            FROM "order".vouchers
            ORDER BY starts_at DESC
            """);
    }

    public record CreateVoucherRequest(String code, String name, String discountType, BigDecimal discountValue,
                                      BigDecimal maxDiscount, BigDecimal minOrderValue, Instant startsAt, Instant expiresAt, int maxUses) {}

    @Transactional
    public UUID create(CreateVoucherRequest req) {
        if (req.code() == null || req.code().isBlank() || req.name() == null || req.name().isBlank() ||
            req.discountValue() == null || req.discountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu voucher không hợp lệ");
        }
        UUID id = UUID.randomUUID();
        db.update("""
            INSERT INTO "order".vouchers(id, code, name, discount_type, discount_value, max_discount, min_order_value, starts_at, expires_at, max_uses, used_count, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 'ACTIVE')
            """, id, req.code().trim().toUpperCase(Locale.ROOT), req.name().trim(),
            req.discountType().toUpperCase(Locale.ROOT), req.discountValue(), req.maxDiscount(),
            req.minOrderValue() != null ? req.minOrderValue() : BigDecimal.ZERO,
            req.startsAt() != null ? java.sql.Timestamp.from(req.startsAt()) : java.sql.Timestamp.from(Instant.now()),
            req.expiresAt() != null ? java.sql.Timestamp.from(req.expiresAt()) : java.sql.Timestamp.from(Instant.now().plusSeconds(86400 * 30)),
            req.maxUses() > 0 ? req.maxUses() : 100);
        return id;
    }

    public void updateStatus(UUID id, String status) {
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
        db.update("UPDATE \"order\".vouchers SET status = ? WHERE id = ?", status, id);
    }
}


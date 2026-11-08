package vn.flora.order;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Unit test cho VoucherService.validate(code, subtotal) – mục 2.3.
 *
 * Kỹ thuật: kiểm thử đường dẫn cơ bản (basis path). Phương thức có 9 nút quyết định
 * => độ phức tạp Cyclomatic V(G) = 9 + 1 = 10 => 10 đường dẫn độc lập (UT-01..UT-10),
 * cộng UT-11 để phủ đủ điều kiện "đã hết hạn" (vế thứ hai của phép ||).
 *
 * Không cần CSDL: JdbcTemplate được thay bằng một FAKE (lớp con ghi đè queryForList)
 * trả về đúng một dòng voucher do test dựng sẵn => đây là kiểm thử đơn vị thuần.
 */
@DisplayName("UT VoucherService.validate – kiểm thử đường dẫn cơ bản")
class VoucherServiceTest {

    /** Dựng 1 dòng voucher giống bảng "order".vouchers. */
    private static Map<String, Object> voucher(String type, String value, String maxDiscount, String minOrder,
                                               Instant startsAt, Instant expiresAt, int maxUses, int used, String status) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", UUID.fromString("60000000-0000-0000-0000-000000000099"));
        row.put("code", "TESTCODE");
        row.put("name", "Voucher kiểm thử");
        row.put("discount_type", type);
        row.put("discount_value", new BigDecimal(value));
        row.put("max_discount", maxDiscount == null ? null : new BigDecimal(maxDiscount));
        row.put("min_order_value", new BigDecimal(minOrder));
        row.put("starts_at", Timestamp.from(startsAt));
        row.put("expires_at", Timestamp.from(expiresAt));
        row.put("max_uses", maxUses);
        row.put("used_count", used);
        row.put("status", status);
        return row;
    }

    /** Voucher hợp lệ mặc định: giảm cố định 50.000đ cho đơn từ 300.000đ (giống FLORANEW). */
    private static Map<String, Object> fixed50k() {
        Instant now = Instant.now();
        return voucher("FIXED", "50000", "50000", "300000",
            now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(30)), 200, 0, "ACTIVE");
    }

    /** FAKE JdbcTemplate: trả về danh sách dòng cố định cho mọi câu truy vấn. */
    private static VoucherService serviceReturning(List<Map<String, Object>> rows) {
        JdbcTemplate fake = new JdbcTemplate() {
            @Override
            public List<Map<String, Object>> queryForList(String sql, Object... args) {
                return rows;
            }
        };
        return new VoucherService(fake);
    }

    private static VoucherService serviceWith(Map<String, Object> row) { return serviceReturning(List.of(row)); }

    private static BigDecimal vnd(String v) { return new BigDecimal(v); }

    @Test
    @DisplayName("UT-01 Mã rỗng/null -> không hợp lệ 'Mã voucher không được để trống'")
    void ut01_blankCode() {
        var svc = serviceWith(fixed50k());
        var r1 = svc.validate(null, vnd("500000"));
        var r2 = svc.validate("   ", vnd("500000"));
        assertFalse(r1.valid());
        assertFalse(r2.valid());
        assertEquals("Mã voucher không được để trống", r1.message());
    }

    @Test
    @DisplayName("UT-02 Mã không tồn tại -> 'Mã voucher không tồn tại'")
    void ut02_codeNotFound() {
        var r = serviceReturning(List.of()).validate("KHONGCO", vnd("500000"));
        assertFalse(r.valid());
        assertEquals("Mã voucher không tồn tại", r.message());
    }

    @Test
    @DisplayName("UT-03 Voucher INACTIVE -> 'Voucher hiện không hoạt động'")
    void ut03_inactive() {
        var row = fixed50k();
        row.put("status", "INACTIVE");
        var r = serviceWith(row).validate("testcode", vnd("500000"));
        assertFalse(r.valid());
        assertEquals("Voucher hiện không hoạt động", r.message());
    }

    @Test
    @DisplayName("UT-04 Voucher chưa tới ngày bắt đầu -> không hợp lệ")
    void ut04_notStartedYet() {
        var row = fixed50k();
        row.put("starts_at", Timestamp.from(Instant.now().plus(Duration.ofDays(1))));
        var r = serviceWith(row).validate("TESTCODE", vnd("500000"));
        assertFalse(r.valid());
        assertEquals("Voucher chưa tới hạn hoặc đã hết hạn", r.message());
    }

    @Test
    @DisplayName("UT-05 Hết lượt sử dụng (used_count = max_uses) -> 'Voucher đã hết lượt sử dụng'")
    void ut05_usedUp() {
        var row = fixed50k();
        row.put("used_count", 200);
        var r = serviceWith(row).validate("TESTCODE", vnd("500000"));
        assertFalse(r.valid());
        assertEquals("Voucher đã hết lượt sử dụng", r.message());
    }

    @Test
    @DisplayName("UT-06 Biên dưới: đơn 299.999đ < tối thiểu 300.000đ -> không hợp lệ")
    void ut06_belowMinimumOrder() {
        var r = serviceWith(fixed50k()).validate("TESTCODE", vnd("299999"));
        assertFalse(r.valid());
        assertEquals("Đơn hàng chưa đạt giá trị tối thiểu 300000đ", r.message());
    }

    @Test
    @DisplayName("UT-07 Biên đúng: đơn 300.000đ, voucher cố định -> giảm 50.000đ")
    void ut07_fixedDiscountAtMinimum() {
        var r = serviceWith(fixed50k()).validate("TESTCODE", vnd("300000"));
        assertTrue(r.valid());
        assertEquals(0, vnd("50000").compareTo(r.discountAmount()));
        assertEquals("TESTCODE", r.code());
    }

    @Test
    @DisplayName("UT-08 Phần trăm chưa chạm trần: 10% của 300.000đ = 30.000đ (trần 50.000đ)")
    void ut08_percentageBelowCap() {
        Instant now = Instant.now();
        var row = voucher("PERCENTAGE", "10", "50000", "200000",
            now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(30)), 500, 0, "ACTIVE");
        var r = serviceWith(row).validate("TESTCODE", vnd("300000"));
        assertTrue(r.valid());
        assertEquals(0, vnd("30000").compareTo(r.discountAmount()));
    }

    @Test
    @DisplayName("UT-09 Phần trăm vượt trần: 10% của 800.000đ = 80.000đ -> chỉ giảm 50.000đ")
    void ut09_percentageCapped() {
        Instant now = Instant.now();
        var row = voucher("PERCENTAGE", "10", "50000", "200000",
            now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(30)), 500, 0, "ACTIVE");
        var r = serviceWith(row).validate("TESTCODE", vnd("800000"));
        assertTrue(r.valid());
        assertEquals(0, vnd("50000").compareTo(r.discountAmount()));
    }

    @Test
    @DisplayName("UT-10 Giảm cố định lớn hơn tiền hàng -> chỉ giảm bằng tiền hàng (không âm)")
    void ut10_discountNotGreaterThanSubtotal() {
        Instant now = Instant.now();
        var row = voucher("FIXED", "100000", null, "0",
            now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(30)), 10, 0, "ACTIVE");
        var r = serviceWith(row).validate("TESTCODE", vnd("60000"));
        assertTrue(r.valid());
        assertEquals(0, vnd("60000").compareTo(r.discountAmount()));
    }

    @Test
    @DisplayName("UT-11 Voucher đã hết hạn -> không hợp lệ (phủ vế thứ hai của điều kiện thời gian)")
    void ut11_expired() {
        var row = fixed50k();
        row.put("starts_at", Timestamp.from(Instant.now().minus(Duration.ofDays(60))));
        row.put("expires_at", Timestamp.from(Instant.now().minus(Duration.ofDays(1))));
        var r = serviceWith(row).validate("TESTCODE", vnd("500000"));
        assertFalse(r.valid());
        assertEquals("Voucher chưa tới hạn hoặc đã hết hạn", r.message());
    }
}

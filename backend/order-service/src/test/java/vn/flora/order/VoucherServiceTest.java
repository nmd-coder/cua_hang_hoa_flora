package vn.flora.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    @Mock
    private JdbcTemplate db;

    @InjectMocks
    private VoucherService voucherService;

    private Map<String, Object> validVoucherData;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mock chuẩn cho một voucher hợp lệ
        validVoucherData = new HashMap<>();
        validVoucherData.put("id", UUID.randomUUID());
        validVoucherData.put("code", "SUMMER50");
        validVoucherData.put("name", "Khuyến mãi mùa hè");
        validVoucherData.put("discount_type", "FIXED_AMOUNT");
        validVoucherData.put("discount_value", new BigDecimal("20000"));
        validVoucherData.put("max_discount", new BigDecimal("20000"));
        validVoucherData.put("min_order_value", new BigDecimal("100000"));
        validVoucherData.put("starts_at", Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        validVoucherData.put("expires_at", Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        validVoucherData.put("max_uses", 100);
        validVoucherData.put("used_count", 10);
        validVoucherData.put("status", "ACTIVE");
    }

    // TC01: Mã voucher trống hoặc null
    @Test
    void validate_CodeIsNullOrEmpty_ReturnsFalse() {
        VoucherService.VoucherValidation result = voucherService.validate("", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertEquals("Mã voucher không được để trống", result.message());
    }

    // TC02: Mã voucher không tồn tại trong CSDL
    @Test
    void validate_CodeNotFound_ReturnsFalse() {
        when(db.queryForList(anyString(), eq("INVALID"))).thenReturn(Collections.emptyList());
        
        VoucherService.VoucherValidation result = voucherService.validate("INVALID", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertEquals("Mã voucher không tồn tại", result.message());
    }

    // TC03: Voucher có trạng thái không hoạt động (INACTIVE)
    @Test
    void validate_StatusInactive_ReturnsFalse() {
        validVoucherData.put("status", "INACTIVE");
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertEquals("Voucher hiện không hoạt động", result.message());
    }

    // TC04: Đơn hàng chưa đạt giá trị tối thiểu
    @Test
    void validate_SubtotalLessThanMin_ReturnsFalse() {
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Hóa đơn 50k < điều kiện 100k
        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("50000"));
        
        assertFalse(result.valid());
        assertTrue(result.message().contains("Đơn hàng chưa đạt giá trị tối thiểu"));
    }

    // TC05: Áp dụng thành công loại giảm giá số tiền cố định (FIXED_AMOUNT)
    @Test
    void validate_Success_FixedAmount() {
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertTrue(result.valid());
        assertEquals(new BigDecimal("20000.00"), result.discountAmount());
        assertEquals("Áp dụng voucher thành công", result.message());
    }

    // TC06: Áp dụng loại giảm % (PERCENTAGE) nhưng bị chặn bởi giới hạn giảm tối đa (max_discount)
    @Test
    void validate_Success_PercentageWithMax() {
        validVoucherData.put("discount_type", "PERCENTAGE");
        validVoucherData.put("discount_value", new BigDecimal("50")); // Giảm 50%
        validVoucherData.put("max_discount", new BigDecimal("30000")); // Tối đa 30k
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Hóa đơn 100k -> 50% là 50k, nhưng max chỉ cho giảm 30k
        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("100000"));
        
        assertTrue(result.valid());
        assertEquals(new BigDecimal("30000.00"), result.discountAmount());
    }
 // TC07: Voucher chưa đến thời gian áp dụng (starts_at > hiện tại)
    @Test
    void validate_StartsInFuture_ReturnsFalse() {
        validVoucherData.put("starts_at", Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertTrue(result.message().contains("chưa đến thời gian")); // Hoặc thông báo lỗi tương ứng trong code của bạn
    }

    // TC08: Voucher đã hết hạn sử dụng (expires_at < hiện tại)
    @Test
    void validate_ExpiredVoucher_ReturnsFalse() {
        validVoucherData.put("expires_at", Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertTrue(result.message().toLowerCase().contains("hết hạn"));
    }

    // TC09: Voucher đã hết lượt sử dụng (used_count >= max_uses)
    @Test
    void validate_MaxUsesReached_ReturnsFalse() {
        validVoucherData.put("used_count", 100);
        validVoucherData.put("max_uses", 100);
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertFalse(result.valid());
        assertTrue(result.message().toLowerCase().contains("hết lượt"));
    }

    // TC10: Loại giảm % (PERCENTAGE) và số tiền giảm CHƯA vượt quá giới hạn tối đa
    @Test
    void validate_Success_PercentageBelowMax() {
        validVoucherData.put("discount_type", "PERCENTAGE");
        validVoucherData.put("discount_value", new BigDecimal("10")); // Giảm 10%
        validVoucherData.put("max_discount", new BigDecimal("50000")); // Tối đa 50k
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Hóa đơn 150k -> giảm 10% là 15k. 15k < 50k -> Phải giảm đúng 15k
        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertTrue(result.valid());
        assertEquals(new BigDecimal("15000.00"), result.discountAmount());
    }

    // TC11: Loại số tiền cố định (FIXED_AMOUNT) nhưng mức giảm lớn hơn cả tổng tiền đơn hàng
    @Test
    void validate_Success_FixedAmountGreaterThanSubtotal() {
        // Mã giảm 200k, nhưng đơn hàng chỉ có 150k
        validVoucherData.put("discount_value", new BigDecimal("200000"));
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertTrue(result.valid());
        // Số tiền giảm tối đa chỉ được bằng giá trị đơn hàng (không được âm tiền)
        assertEquals(new BigDecimal("150000.00"), result.discountAmount());
    }

    // TC12: Kiểm tra biên (Boundary Value) - Đơn hàng BẰNG CHÍNH XÁC giá trị tối thiểu
    @Test
    void validate_Success_SubtotalExactlyEqualsMinOrder() {
        validVoucherData.put("min_order_value", new BigDecimal("100000"));
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Hóa đơn vừa vặn 100k
        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("100000"));
        
        assertTrue(result.valid());
        assertEquals("Áp dụng voucher thành công", result.message());
    }

    // TC13: Voucher không giới hạn số lượt sử dụng (max_uses là null)
    @Test
    void validate_Success_InfiniteUses() {
        validVoucherData.put("max_uses", null); 
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        assertTrue(result.valid());
    }

    // TC14: Nhánh mặc định (Fallback) khi discount_type không xác định
    @Test
    void validate_Success_UnknownDiscountType() {
        validVoucherData.put("discount_type", "UNKNOWN_TYPE");
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        VoucherService.VoucherValidation result = voucherService.validate("SUMMER50", new BigDecimal("150000"));
        
        // Vẫn hợp lệ nhưng số tiền giảm mặc định là 0
        assertTrue(result.valid());
        assertEquals(BigDecimal.ZERO, result.discountAmount());
    }
 // TC15: Lỗ hổng không kiểm tra tham số đầu vào (Subtotal là null)
    // Thực tế: Code thường quên kiểm tra (subtotal == null). Khi so sánh điều kiện (subtotal.compareTo(min_order_value)), chương trình sẽ bị văng lỗi NullPointerException.
    @Test
    void validate_Unhandled_SubtotalIsNull_ThrowsException() {
        // Giả lập voucher tồn tại hợp lệ
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Kỳ vọng hàm validate sẽ bị sập (ném ra NullPointerException) vì không lường trước người dùng truyền subtotal = null
        assertThrows(NullPointerException.class, () -> {
            voucherService.validate("SUMMER50", null);
        });
    }

    // TC16: Lỗ hổng dữ liệu bẩn từ cơ sở dữ liệu (Trường dữ liệu quan trọng bị NULL)
    // Thực tế: Nếu database bị lỗi khiến cột "discount_value" bị NULL, khi lấy dữ liệu ra từ Map<String, Object> và gọi .toString() hoặc thực hiện tính toán, hệ thống sẽ sập.
    @Test
    void validate_Unhandled_DatabaseMissingCriticalData_ThrowsException() {
        // Cố tình tạo ra dữ liệu "bẩn" từ CSDL: Giá trị giảm giá bị thiếu (NULL)
        validVoucherData.put("discount_value", null);
        when(db.queryForList(anyString(), eq("SUMMER50"))).thenReturn(List.of(validVoucherData));

        // Kỳ vọng code sẽ crash (ném ra NullPointerException hoặc NumberFormatException) do thiếu bước kiểm tra null khi đọc dữ liệu từ DB
        assertThrows(Exception.class, () -> {
            voucherService.validate("SUMMER50", new BigDecimal("150000"));
        });
    }
}
# Kiến trúc Hệ thống FLORA (Microservices Architecture)

## 1. Nguyên tắc thiết kế

1. **Database-per-service**:
   - Mỗi microservice sở hữu toàn bộ bảng và dữ liệu trong schema riêng biệt của mình (`identity`, `catalog`, `inventory`, `order`, `payment`, `customer`).
   - Tuyệt đối không có Foreign Key hoặc JOIN trực tiếp xuyên schema.
   - Dữ liệu tham chiếu giữa các service (như `product_id`, `user_id`, `order_id`) được lưu dưới dạng giá trị độc lập và xác minh qua REST API.

2. **Bảo toàn dữ liệu (Snapshot Pattern)**:
   - Khi khách hàng tạo đơn hàng, `order-service` lưu trữ snapshot toàn bộ thông tin tại thời điểm mua: `sku_snapshot`, `name_snapshot`, `unit_price`, `image_snapshot`, `address_snapshot`.
   - Nếu quản trị viên thay đổi giá hoặc thông tin hoa trong `catalog-service`, lịch sử đơn hàng cũ hoàn toàn không bị ảnh hưởng.

3. **Giao dịch phân tán & Tính Idempotent (Idempotency Pattern)**:
   - Các thao tác tạo đơn (`POST /api/orders`) yêu cầu header `Idempotency-Key`.
   - Nếu client gửi lại cùng một key với cùng payload, hệ thống trả về kết quả đơn hàng đã tạo thay vì tạo đơn thứ hai hoặc giữ tồn hai lần.
   - Thao tác giữ tồn kho (`reserve`) và giải phóng tồn kho (`release`) trong `inventory-service` hoạt động nguyên tử và bảo đảm tính idempotent.

4. **Xử lý số liệu tiền tệ**:
   - Sử dụng kiểu dữ liệu `NUMERIC(15,2)` trong PostgreSQL.
   - Sử dụng `java.math.BigDecimal` trong toàn bộ mã nguồn Java.
   - Tuyệt đối không sử dụng `float` hay `double` trong các phép tính chiết khấu, thuế, phí vận chuyển và tổng tiền.

5. **Phân quyền và bảo mật**:
   - Mật khẩu người dùng được băm an toàn bằng thuật toán `BCrypt` với cost factor = 12.
   - Phân quyền theo vai trò (`GUEST`, `CUSTOMER`, `STAFF`, `ADMIN`) được kiểm tra nghiêm ngặt tại backend service.
   - Các API trao đổi nội bộ giữa các service được bảo vệ bằng header `X-Internal-Key`.

## 2. Danh mục Schema và Bảng dữ liệu

### Schema `identity`
- `users`: Tài khoản cơ sở (`id`, `email`, `phone`, `status`, `created_at`).
- `credentials`: Mật khẩu mã hóa BCrypt (`user_id`, `password_hash`).
- `user_roles`: Phân quyền vai trò (`user_id`, `role`).
- `sessions`: Phiên đăng nhập hoạt động (`token`, `user_id`, `expires_at`).

### Schema `catalog`
- `categories`: Danh mục sản phẩm (Bó hoa, Giỏ hoa, Kệ khai trương, Hoa cưới).
- `products`: Sản phẩm hoa tươi (`id`, `sku`, `name`, `slug`, `price`, `status`, `weight_g`, `dimensions`).
- `product_images`: Thư viện ảnh sản phẩm (`product_id`, `url`, `is_primary`, `sort_order`).
- `occasions`: Dịp tặng hoa (Sinh nhật, Tình yêu, Chúc mừng, Khai trương, Chia buồn).
- `product_occasions`: Bảng liên kết sản phẩm - dịp tặng.
- `flower_types`: Loại hoa chính (Hồng Ecuador, Cẩm tú cầu, Baby, Hướng dương, Tulip, Lan hồ điệp).
- `product_flower_types`: Bảng liên kết sản phẩm - loại hoa.
- `colors`: Màu sắc chủ đạo (Đỏ, Hồng, Trắng, Vàng, Xanh tím, Cam đào).
- `product_colors`: Bảng liên kết sản phẩm - màu sắc.

### Schema `inventory`
- `stock`: Quản lý tồn kho thực tế (`item_id`, `on_hand`, `reserved`, `updated_at`).
- `stock_reservations`: Phiên giữ hàng cho đơn hàng (`order_id`, `item_id`, `quantity`, `status`).
- `reservation_batches`: Lô giữ hàng nguyên tử nhiều sản phẩm.
- `stock_movements`: Lịch sử biến động kho (`item_id`, `change_qty`, `reason`, `reference_id`, `created_at`).

### Schema `order`
- `checkout_quotes`: Báo giá tạm tính (`user_id`, `subtotal`, `discount_amount`, `shipping_fee`, `total`, `expires_at`).
- `checkout_intents`: Ý định đặt hàng kết nối với quote.
- `checkout_requests`: Lưu vết request Idempotency-Key chống trùng lặp.
- `orders`: Đơn hàng chính thức (`id`, `order_code`, `user_id`, `status`, `total`, `recipient_name`, `recipient_phone`, `delivery_date`, `delivery_time_slot`, `card_message`).
- `order_items`: Chi tiết các mặt hàng trong đơn hàng kèm snapshot thông tin.
- `order_status_history`: Lịch sử các bước chuyển trạng thái đơn hàng.
- `vouchers`: Mã giảm giá khuyến mãi (`code`, `discount_type`, `discount_value`, `min_order_amount`, `max_discount_amount`, `max_uses`, `used_count`).
- `voucher_redemptions`: Lịch sử áp dụng mã voucher cho từng đơn hàng.

### Schema `payment`
- `payments`: Giao dịch thanh toán (`id`, `order_id`, `method`, `amount`, `status`, `transaction_code`).
- `refunds`: Lịch sử giao dịch hoàn tiền (`id`, `order_id`, `payment_id`, `amount`, `reason`, `status`).

### Schema `customer`
- `customer_profiles`: Hồ sơ khách hàng (`user_id`, `full_name`, `avatar_url`).
- `customer_addresses`: Sổ địa chỉ giao nhận hoa (`user_id`, `recipient_name`, `recipient_phone`, `province`, `district`, `ward`, `address_detail`, `is_default`).
- `wishlists`: Danh sách sản phẩm hoa yêu thích (`user_id`, `product_id`).
- `reviews`: Đánh giá và số sao chất lượng hoa từ người mua (`order_item_id`, `product_id`, `user_id`, `rating`, `comment`).

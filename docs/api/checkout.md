# Báo giá checkout cục bộ

POST /api/checkout/quotes qua gateway, Authorization: Bearer <token>.
JSON: items [{productId: UUID, quantity: 1..99}], recipientName, recipientPhone, address, deliveryDate (YYYY-MM-DD), timeSlot, cardMessage.
GET /api/checkout/quotes/{id}: chỉ chủ báo giá và trước hạn 10 phút.

Order xác nhận giá và trạng thái bằng Catalog, dùng BigDecimal, lưu snapshot JSONB và trả subtotal, shippingFee, total, currency=VND, shippingMode=MOCK. Phí mock được cấu hình bằng MOCK_SHIPPING_FEE, mặc định 30000 đồng. Không gọi GHN thật; khung giờ là thông tin đề nghị, chưa xác nhận công suất giao.

Báo giá không giữ hàng, không sử dụng voucher, không tạo đơn và không xác nhận thanh toán. Bước POST /api/orders đã được bổ sung để giữ tồn theo lô và tạo đơn COD chống trùng; xem cod-orders.md. Chỉ phản hồi tạo đơn PENDING mới xác nhận đơn đã được tiếp nhận.

Migration V2__checkout_quotes.sql tạo dữ liệu thuộc Order, không có FK xuyên service.
# API giỏ hàng đang phát triển

Qua gateway: GET /api/cart, PUT /api/cart/items/{UUID} với JSON {"quantity": 1}, DELETE cùng đường dẫn. Cần Authorization: Bearer <token>; Order kiểm tra token qua Identity và lấy user id từ phản hồi đã xác thực. Quantity từ 1 đến 99, PUT đặt số lượng và an toàn khi gửi lại. Mỗi tài khoản có giỏ riêng. Identity không khả dụng thì thao tác thất bại, không tin ID do client cung cấp.

Giỏ chưa kiểm tra sản phẩm có tồn tại, giá hoặc tồn; bắt buộc xác nhận lại trước checkout. Giao diện giỏ đã được bổ sung nhưng chưa được kiểm thử đầu cuối trên trình duyệt; chưa có tạo đơn. Không dùng cho vận hành.
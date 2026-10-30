# Đặt đơn COD cục bộ

POST /api/orders: Authorization Bearer, Idempotency-Key từ 16–100 ký tự chữ/số/_/-, JSON {"quoteId":"UUID"}. Cùng user và khóa chỉ tạo một tiến trình; cùng khóa khác quote trả 409. Mỗi quote chỉ dùng cho một tiến trình. Báo giá phải còn hạn và sản phẩm/giá được kiểm tra lại trước khi nhận tiến trình. Báo giá cũ thiếu SKU phải tạo mới.

Intent PREPARING được ghi bền trước khi gọi Inventory. Inventory giữ toàn bộ dòng trong một transaction; thất bại không giữ một phần. Khi kho xác nhận thành công, Order ghi đơn PENDING, dòng snapshot, trạng thái thanh toán UNPAID và lịch sử trong một transaction cục bộ. Đây là COD, chưa thu tiền. GET /api/orders và /api/orders/{id} kiểm tra chủ đơn.

Timeout giữ PREPARING; thử lại cùng khóa. Worker mỗi 30 giây thử tối đa 10 intent cũ, không cần đọc lại báo giá đã hết hạn vì intent đã được tiếp nhận và chụp trước đó. Nếu local commit thất bại sau khi kho đã giữ, cùng order ID khử trùng lần giữ sau. Kho trả 409 thì intent FAILED, không tạo đơn. Không có ACID xuyên service.

INTERNAL_API_KEY dài tối thiểu 32 ký tự bảo vệ API kho, chỉ trao đổi trong mạng Docker, không công khai qua gateway. .env cục bộ bị gitignore, .env.example chỉ chứa giá trị ví dụ.

Giả định local: chủ đơn chỉ được hủy đơn PENDING và UNPAID. POST /api/orders/{id}/cancel ghi CANCEL_PENDING, gọi kho giải phóng tồn idempotent, rồi ghi CANCELLED và lịch sử. Nếu kho lỗi/timeout, worker thử lại; cùng yêu cầu hủy gửi lại an toàn. Đơn khác hoặc đã thanh toán cần chính sách riêng. Giới hạn: chưa có chuyển trạng thái quản trị, tiêu thụ tồn theo quy trình giao hàng, thu COD, hoàn tiền, xác nhận công suất giao hoặc voucher. PENDING giữ tồn đến khi có quy trình xử lý tiếp; expires_at ở reservation chưa có worker tự nhả, không được tự diễn giải là đã nhả sau 15 phút. Phí và giao hàng là MOCK. Giỏ giữ nguyên sau đặt đơn, để tránh xóa thay đổi đồng thời của người dùng; một lần mua mới cần báo giá mới. Các luồng này dành cho trình diễn local.
## Cập nhật trạng thái đang triển khai
POST /api/orders/{id}/status nhận {"status":"PROCESSING"}: chỉ STAFF/ADMIN xác thực qua Identity, từ PENDING. {"status":"RECEIVED"}: chỉ chủ đơn, từ COMPLETED. Mỗi chuyển trạng thái hợp lệ dùng khóa dòng và ghi lịch sử với actor. Bước SHIPPED chưa được mở vì chưa nối tiêu thụ tồn và vận chuyển. Không có API cấp quyền hoặc tài khoản STAFF/ADMIN mẫu.

Đã kiểm thử quy tắc vai trò và build gateway; chưa kiểm thử các endpoint mới với PostgreSQL/HTTP vì Docker engine không phản hồi. Luồng hủy đã kiểm thử ở lớp Order với PostgreSQL và kho giả, chưa kiểm thử HTTP với Inventory thật sau thay đổi. Đây là các giới hạn cần kiểm tra trước nghiệm thu.
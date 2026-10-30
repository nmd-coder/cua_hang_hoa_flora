# Giả định, quyết định và API dự kiến

- Bắt đầu dữ liệu mới; chưa chuyển StarShop. Đơn vị tồn dự kiến là sản phẩm hoàn thiện, chưa có biến thể. Cần xác nhận trước vận hành.
- Đăng nhập bắt buộc khi đặt hàng. Giá và phí do server tính. Khi báo phí lỗi, checkout phải báo lỗi; không mặc định 0.
- COD là luồng đầu tiên; MoMo/GHN chỉ được bật sau khi cấu hình và xác minh hợp đồng sandbox. Không coi return URL hay mock là bằng chứng giao dịch thật.
- Trạng thái chính dự kiến PENDING → PROCESSING → SHIPPED → COMPLETED → RECEIVED. Quyền hủy, hết hạn giữ, hoàn voucher/tồn và xử lý hoa đã chế biến cần xác nhận. Không giả định hoàn tiền thành công khi hủy.
- Khu vực, công suất, khung giờ giao, thời hạn lưu PII và quy tắc voucher cần chủ cửa hàng xác nhận. Chưa có quyết định thời hạn giữ tồn hay giữ voucher.

## Hợp đồng dự kiến, chưa triển khai

Gateway /api: /auth, /products, /inventory, /cart, /orders, /payments, /customers. Các API riêng tư cần xác thực và kiểm tra chủ sở hữu hoặc quyền STAFF/ADMIN ở service. Lỗi dự kiến JSON có code, message, correlationId; không tiết lộ dữ liệu cá nhân. POST /orders cần Idempotency-Key, cùng key khác payload trả 409. Webhook xác minh chữ ký, giao dịch và số tiền theo hợp đồng provider; khử trùng lặp. Swagger hiện chỉ là nền tảng và chưa mô tả nghiệp vụ.
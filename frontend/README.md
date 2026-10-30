# Frontend FLORA local
Next.js App Router với tài khoản, giỏ hàng, thông tin giao, báo giá, xác nhận COD, danh sách và chi tiết đơn. Server Actions dùng cookie HttpOnly SameSite Strict, không lưu token vào localStorage.

Docker: chạy docker compose up -d --build từ thư mục gốc và mở http://127.0.0.1:3000. Nếu chạy trực tiếp, đặt GATEWAY_URL=http://127.0.0.1:8080. Build: npm ci rồi npm run build.

Kiểm thử Edge: npx playwright test; Edge cần được cài sẵn. Luồng đăng ký đến xem đơn COD PENDING đã chạy đạt trên local. File tests/cod.spec.ts dọn tài khoản, đơn và lượng giữ tồn tổng hợp sau test.

Khách có thể yêu cầu hủy đơn PENDING chưa thu tiền; khi kho đang xử lý, đơn ở trạng thái CANCEL_PENDING. Chưa có khu vực quản trị, cập nhật giao/nhận, thu COD hoặc tích hợp MoMo/GHN thật. Phí giao hiện là mock, khung giờ đề nghị chưa xác nhận công suất cửa hàng.
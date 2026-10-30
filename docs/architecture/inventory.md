# Inventory local
Giữ, nhả và trừ một mặt hàng dùng khóa dòng stock. BatchService giữ một lô trong một transaction; thiếu một mặt hàng thì rollback toàn lô. Batch khóa theo order id, khóa sản phẩm theo thứ tự UUID để giảm deadlock. Replay cùng order và payload không tăng reserved. Release ghi tombstone để chặn reserve đến muộn.

API /internal/reservations/{orderId} PUT/DELETE nhận X-Internal-Key, chỉ dùng trong mạng Docker và không được gateway công khai. Stock mẫu V3 khởi tạo 20 đơn vị cho mỗi sản phẩm demo nếu chưa có dòng tồn, không sửa tồn đã tồn tại.

Kiểm thử PostgreSQL đã đạt tranh tồn cuối, reserve/commit/release idempotent, batch rollback và chặn reserve sau release. Luồng COD HTTP với kho thật cũng đạt, tồn chỉ giữ một lần khi replay.

Chưa có nghiệp vụ quản trị điều chỉnh tồn, hủy đơn/giao hàng hoặc worker nhả theo expires_at. Đơn PENDING giữ tồn cho đến khi có quy trình tiếp theo. Không tự coi reservation hết hạn là đã nhả.
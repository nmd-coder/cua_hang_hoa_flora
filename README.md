# FLORA — Cửa Hàng Hoa Tươi Trực Tuyến (Microservices Architecture)

Hệ thống bán hoa tươi trực tuyến **FLORA** được thiết kế và xây dựng theo kiến trúc **Microservices** hiện đại, kết hợp giữa backend **Spring Boot** (Java 21) mạnh mẽ, an toàn và frontend **Next.js** (App Router, React 19, TypeScript) tinh tế, tối ưu trải nghiệm khách hàng lẫn nhân viên và quản trị viên.

---

## 1. Kiến trúc hệ thống & Danh mục Dịch vụ

Hệ thống tuân thủ nguyên tắc độc lập cơ sở dữ liệu: **Không dùng chung bảng hay Foreign Key xuyên service (No cross-service DB constraints)**. Giao tiếp giữa các service diễn ra qua REST API chuẩn hóa với mã xác thực nội bộ `X-Internal-Key`.

```
               [ Khách hàng / Nhân viên / Quản trị viên ]
                                   │
                                   ▼
                   [ Frontend Next.js - Port 3000 ]
                                   │
                                   ▼
                  [ API Gateway (Spring Boot) - Port 8080 ]
                                   │
     ┌─────────────┬───────────────┼───────────────┬─────────────┬─────────────┐
     ▼             ▼               ▼               ▼             ▼             ▼
[Identity]     [Catalog]      [Inventory]       [Order]      [Payment]    [Customer]
:8081          :8082          :8083             :8084        :8085        :8086
Schema:        Schema:        Schema:           Schema:      Schema:      Schema:
identity       catalog        inventory         order        payment      customer
     │             │               │               │             │             │
     └─────────────┴───────────────┴───────────────┴─────────────┴─────────────┘
                                   │
                                   ▼
             [ PostgreSQL Database: flora - Port 5432 ]
```

### 1.1. Bảng phân chia trách nhiệm Microservices

| Service | Port | Database Schema | Nhiệm vụ chính & Nghiệp vụ cốt lõi |
| :--- | :---: | :---: | :--- |
| **api-gateway** | `8080` | *(Không lưu DB)* | Cổng điều hướng tập trung (Reverse Proxy), định tuyến request, xử lý CORS, phân phối tải tới các microservices phía sau. |
| **identity-service** | `8081` | `identity` | Đăng ký, đăng nhập, cấp và thu hồi phiên xác thực (`Bearer session tokens`), phân quyền theo vai trò (`CUSTOMER`, `STAFF`, `ADMIN`), mã hóa mật khẩu với BCrypt 12 rounds. |
| **catalog-service** | `8082` | `catalog` | Danh mục hoa tươi, phân loại đa chiều (Dịp tặng, Loại hoa, Màu sắc, Khoảng giá), kích thước, thư viện hình ảnh, CRUD quản trị sản phẩm. |
| **inventory-service** | `8083` | `inventory` | Quản lý tồn kho hoa tươi, giữ hàng nguyên tử (`atomic stock reservation`), nhả hàng idempotent khi hủy đơn, lưu vết biến động kho (`stock_movements`) và điều chỉnh tồn kho. |
| **order-service** | `8084` | `order` | Giỏ hàng, báo giá giao hoa (`quotes`), kiểm tra voucher giảm giá, tạo đơn COD/trực tuyến với Idempotency-Key chống trùng lặp, quản lý snapshot giá/sản phẩm tại thời điểm mua, vòng đời trạng thái đơn hàng (`PENDING` -> `PROCESSING` -> `SHIPPED` -> `COMPLETED` -> `RECEIVED` / `CANCELLED`), thống kê KPI doanh thu. |
| **payment-service** | `8085` | `payment` | Quản lý giao dịch thanh toán COD, adapter tích hợp MoMo Sandbox (giả lập thanh toán trực tuyến, QR, webhook callback, xác minh chữ ký và hoàn tiền refund). |
| **customer-service** | `8086` | `customer` | Hồ sơ cá nhân khách hàng, sổ địa chỉ nhận hoa giao hàng (hỗ trợ địa chỉ mặc định), danh sách hoa yêu thích (`wishlist`), đánh giá sản phẩm có kèm điều kiện mua hàng. |

---

## 2. Công nghệ sử dụng & Phiên bản

- **Backend**:
  - Java 21 LTS (tương thích Java 26)
  - Spring Boot `3.4.5`
  - Spring Security & Spring Web (RESTful APIs)
  - Spring Data JPA & Hibernate
  - Flyway Migration (quản lý phiên bản cấu trúc database riêng cho từng schema)
  - PostgreSQL JDBC Driver
  - Apache Maven `3.9+`
  - Xử lý tiền tệ: Hoàn toàn sử dụng `NUMERIC(15,2)` trong PostgreSQL và `java.math.BigDecimal` trong Java (không dùng float/double).
- **Frontend**:
  - Next.js `15.5.28` (App Router)
  - React `19.0.0`
  - TypeScript `5.8.3`
  - Thiết kế Responsive (Mobile, Tablet, Desktop)
  - Bảng màu nhận diện FLORA: Kem trang nhã (`#FAF8F4`), Xanh lá rừng (`#315B45`), Hồng phấn hoa (`#D6A6A0`), Đen tro sang trọng (`#25332B`).
- **Database & Hạ tầng**:
  - PostgreSQL 16
  - Docker & Docker Compose v2

---

## 3. Tài khoản thử nghiệm mặc định (Seed Accounts)

Hệ thống đã chuẩn bị sẵn tài khoản mẫu cho đầy đủ các vai trò nghiệp vụ (Mật khẩu đều là `Password123@`):

| Vai trò (Role) | Email đăng nhập | Mật khẩu | Quyền hạn & Chức năng kiểm thử |
| :--- | :--- | :--- | :--- |
| **CUSTOMER** | `customer@flora.vn` | `Password123@` | Xem danh mục, lọc hoa, đặt hàng COD, áp mã voucher, quản lý sổ địa chỉ, wishlist, hủy đơn `PENDING`, xác nhận đã nhận hoa `RECEIVED`, gửi đánh giá sản phẩm. |
| **STAFF** | `staff@flora.vn` | `Password123@` | Truy cập trang Nhân viên (`/nhan-vien`), tiếp nhận đơn hàng, duyệt chuyển trạng thái `PENDING` ➔ `PROCESSING` ➔ `SHIPPED` ➔ `COMPLETED`. |
| **ADMIN** | `admin@flora.vn` | `Password123@` | Toàn quyền Bảng điều khiển Quản trị (`/admin`): xem KPI doanh thu & đơn hàng, thêm/sửa sản phẩm (`/admin/san-pham`), điều chỉnh tồn kho (`/admin/ton-kho`), tạo và quản lý mã Voucher (`/admin/vouchers`), xử lý hủy đơn và hoàn tiền (`/admin/don-hang`). |

---

## 4. Danh sách Swagger UI & API Endpoints

Mỗi service cung cấp tài liệu OpenAPI / Swagger UI đầy đủ và độc lập:

| Dịch vụ | Địa chỉ Swagger UI | Endpoint API Docs |
| :--- | :--- | :--- |
| **API Gateway** | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/v3/api-docs` |
| **Identity Service** | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| **Catalog Service** | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| **Inventory Service** | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |
| **Order Service** | `http://localhost:8084/swagger-ui.html` | `http://localhost:8084/v3/api-docs` |
| **Payment Service** | `http://localhost:8085/swagger-ui.html` | `http://localhost:8085/v3/api-docs` |
| **Customer Service** | `http://localhost:8086/swagger-ui.html` | `http://localhost:8086/v3/api-docs` |

---

## 5. Hướng dẫn cài đặt và khởi chạy

### Cách 1: Khởi chạy bằng Docker Compose (Khuyên dùng)

1. **Chuẩn bị cấu hình môi trường**:
   Sao chép file `.env.example` thành `.env` (hoặc sử dụng `.env` đã có sẵn):
   ```bash
   cp .env.example .env
   ```
2. **Khởi động toàn bộ cụm dịch vụ**:
   ```powershell
   docker compose up -d --build
   ```
3. **Kiểm tra trạng thái các container**:
   ```powershell
   docker compose ps
   ```
4. **Truy cập ứng dụng**:
   - Giao diện Website FLORA: [http://localhost:3000](http://localhost:3000)
   - API Gateway: [http://localhost:8080](http://localhost:8080)
   - Cổng Quản trị viên: [http://localhost:3000/admin](http://localhost:3000/admin)
   - Cổng Nhân viên xử lý đơn: [http://localhost:3000/nhan-vien](http://localhost:3000/nhan-vien)

### Cách 2: Chạy trực tiếp trên máy phát triển (Local Development)

Nếu muốn chạy từng service riêng biệt mà không dùng container cho ứng dụng:

1. **Chạy PostgreSQL**:
   Đảm bảo PostgreSQL 16 đang chạy tại cổng `5432`, có database tên `flora` với user `flora` và password theo cấu hình trong `.env`.
2. **Khởi chạy lần lượt các backend services**:
   Tại mỗi thư mục `backend/<service>` (ví dụ `identity-service`, `catalog-service`, ...):
   ```powershell
   mvn spring-boot:run
   ```
3. **Khởi chạy Frontend**:
   Tại thư mục `frontend`:
   ```powershell
   npm install
   npm run dev
   ```
   Website sẽ sẵn sàng tại `http://localhost:3000`.

---

## 6. Các chức năng và Màn hình chính

### 6.1. Giao diện Khách hàng (Customer Interface)
- **Trang chủ (`/`)**:
  - Banner câu chuyện thương hiệu FLORA, danh mục hoa nổi bật (Bó hoa, Giỏ hoa, Kệ khai trương, Hoa cưới).
  - Bộ công cụ tìm hoa nhanh theo Dịp tặng và Ngân sách.
  - Sản phẩm bán chạy tuyển chọn, cam kết dịch vụ (Giao 2h, Ảnh thật trước khi giao, Hoa tươi 3 ngày).
  - Cảm nhận từ khách hàng thực tế và Footer thông tin chính sách.
- **Danh sách sản phẩm (`/san-pham`)**:
  - Bộ lọc đa tiêu chí linh hoạt: Theo Dịp tặng, Loại hoa (Hồng Ecuador, Cẩm tú cầu, Baby, Tulip, Hướng dương), Màu sắc và Khoảng giá.
  - Tìm kiếm theo từ khóa tên hoa/mô tả.
  - Sắp xếp linh hoạt: Mới nhất, Giá tăng dần, Giá giảm dần.
- **Chi tiết sản phẩm (`/san-pham/[id]`)**:
  - Thư viện hình ảnh sắc nét.
  - Chi tiết kích thước (Dài x Rộng x Cao, Khối lượng), thông điệp ý nghĩa hoa.
  - Chính sách đổi trả và bù hoa tương đương nếu hoa theo mùa bị khan hiếm.
  - Nút thêm vào giỏ, nút Mua ngay, nút Yêu thích (Wishlist).
  - Danh sách đánh giá sao và bình luận thực tế từ người mua.
  - Khối hoa tươi tương tự gợi ý.
- **Giỏ hàng (`/gio-hang`)**:
  - Danh sách hoa đã chọn, tăng/giảm số lượng theo thời gian thực, xóa sản phẩm.
  - Tóm tắt tổng tiền hàng minh bạch.
- **Đặt hàng / Checkout (`/thanh-toan`)**:
  - Thông tin người đặt và người nhận hoa.
  - Sổ địa chỉ tiện lợi (tự động điền địa chỉ mặc định của khách).
  - Chọn ngày giao và khung giờ giao hoa (08:00 - 12:00, 13:00 - 17:00, 17:00 - 20:00).
  - Lời nhắn viết tay gửi kèm trên thiệp mừng.
  - Ô nhập mã Voucher giảm giá (`FLORA10`, `FLORANEW`, `FLORALOVE`).
  - Lựa chọn phương thức thanh toán: COD hoặc MoMo Trực tuyến.
- **Xác nhận đơn hàng (`/thanh-toan/[id]`)**:
  - Hiển thị chi tiết bảng tính tiền (Tiền hàng, Giảm giá voucher, Phí ship, Tổng COD).
  - Tùy chọn giả lập quét mã MoMo Sandbox trực tiếp.
- **Lịch sử đơn hàng (`/don-hang`) & Chi tiết đơn (`/don-hang/[id]`)**:
  - Bộ lọc phân loại trạng thái: `Tất cả`, `Chờ xử lý`, `Đang cắm hoa`, `Đang giao`, `Đã giao`, `Đã nhận`, `Đã hủy`.
  - Khách hàng có thể tự Hủy đơn khi đơn còn ở trạng thái `PENDING` (hệ thống sẽ tự động giải phóng tồn kho).
  - Khách hàng bấm Xác nhận "Đã nhận được hoa" khi đơn đạt `COMPLETED` để chuyển sang `RECEIVED`.
  - Nút "Đánh giá sản phẩm" cho từng sản phẩm trong đơn đã giao thành công.
- **Trang Tài khoản (`/tai-khoan`)**:
  - Đăng ký tài khoản mới và Đăng nhập bảo mật.
  - Phím bấm Đăng nhập nhanh một chạm (Quick Demo Login) dành cho cả 3 vai trò: Khách hàng, Nhân viên, Admin.
  - Quản lý hồ sơ cá nhân và Sổ địa chỉ giao hàng (Thêm địa chỉ mới, Xóa, Thiết lập địa chỉ mặc định).
  - Danh sách sản phẩm hoa yêu thích (Wishlist).

### 6.2. Giao diện Nhân viên (`/nhan-vien`)
- Dành riêng cho nhân viên cửa hàng và thợ cắm hoa.
- Theo dõi toàn bộ danh sách đơn đặt theo thời gian thực.
- Cập nhật tiến độ cắm hoa và vận chuyển:
  - Tiếp nhận đơn: `PENDING` ➔ `PROCESSING` (Bắt đầu cắm hoa).
  - Bàn giao shipper: `PROCESSING` ➔ `SHIPPED` (Đang giao hàng).
  - Giao thành công: `SHIPPED` ➔ `COMPLETED` (Đã giao tới tay người nhận).

### 6.3. Giao diện Quản trị viên (`/admin`)
- **Bảng điều khiển KPI (`/admin`)**:
  - Thống kê tổng doanh thu thực tế.
  - Số lượng đơn theo từng trạng thái (Đơn mới, Đang xử lý, Đang giao, Đã giao, Đã hủy).
  - Cảnh báo tồn kho thấp cho các loại hoa sắp hết.
- **Quản lý sản phẩm (`/admin/san-pham`)**:
  - Thêm mới sản phẩm hoa tươi (Tên, SKU, Danh mục, Giá bán, Ảnh minh họa, Kích thước).
  - Bật/Tắt trạng thái mở bán của sản phẩm (`ACTIVE` / `INACTIVE`).
- **Quản lý kho hoa (`/admin/ton-kho`)**:
  - Xem số lượng tồn thực tế (`on_hand`), lượng đang giữ cho đơn chờ (`reserved`), và tồn khả dụng (`available`).
  - Biểu mẫu điều chỉnh tồn kho (Nhập thêm hoa, Điều chỉnh hư hỏng/héo, Kiểm kê thực tế) có ghi nhận lý do và lưu vết lịch sử biến động (`stock_movements`).
- **Quản lý Voucher (`/admin/vouchers`)**:
  - Danh sách mã khuyến mãi, tỷ lệ giảm %, số tiền giảm tối đa, giá trị đơn tối thiểu, số lượt đã dùng và hạn sử dụng.
  - Biểu mẫu tạo mới mã Voucher khuyến mãi cho các chiến dịch.
- **Quản lý đơn hàng & Hoàn tiền (`/admin/don-hang`)**:
  - Bộ lọc đơn hàng nâng cao theo mã đơn và trạng thái.
  - Can thiệp chuyển trạng thái đơn hàng linh hoạt.
  - Xử lý hoàn tiền (`Refund`) đối với các đơn hàng bị hủy hoặc có sự cố thanh toán.

---

## 7. Các chức năng Giả lập (Mock / Simulated Features)

Nhằm đảm bảo an toàn tuyệt đối và tính độc lập của môi trường phát triển cục bộ (không phụ thuộc bên thứ 3 và không đưa thông tin bí mật lên git):

1. **MoMo Sandbox Mock Adapter**:
   - Được đánh dấu rõ ràng là cơ chế mô phỏng (`[MÔ PHỎNG / MOCK]`).
   - Cung cấp tính năng giả lập tạo mã QR/liên kết thanh toán MoMo Sandbox.
   - Hỗ trợ endpoint `/api/payments/momo/simulate` để mô phỏng sự kiện khách hàng quét mã và thanh toán thành công, gửi webhook callback cập nhật trạng thái thanh toán sang `PAID` mà không cần tài khoản doanh nghiệp MoMo thật.
2. **GHN Shipping Fee Mock**:
   - Phí vận chuyển mặc định tạm tính là `30.000 VNĐ`.
   - Trong quá trình phát triển, cước phí được tính toán nội bộ để phản ánh chính xác cấu trúc bảng báo giá (`checkout_quotes`) trước khi tích hợp API chính thức của Giao Hàng Nhanh / Ahamove.

---

## 8. Hướng dẫn chạy Bộ kiểm thử (Automated Tests)

### Chạy kiểm thử tự động toàn bộ hệ thống:
```powershell
& scripts/test.ps1
```
Kịch bản `test.ps1` sẽ tự động:
1. Xác thực cú pháp file cấu hình Docker Compose `docker-compose.yml`.
2. Chạy toàn bộ Unit & Integration tests bằng Maven cho cả 7 microservices (`api-gateway`, `identity-service`, `catalog-service`, `inventory-service`, `order-service`, `payment-service`, `customer-service`).
3. Cài đặt thư viện và build kiểm tra tính hợp lệ của toàn bộ mã nguồn Next.js TypeScript (`npm run build`).

### Chạy kịch bản kiểm thử luồng COD với Database thực tế:
Khi docker compose đang chạy, thực thi:
```powershell
& scripts/test-cod-http.ps1
```
Kịch bản sẽ tự động:
- Đăng ký và đăng nhập tài khoản khách hàng thử nghiệm ngẫu nhiên.
- Tạo báo giá giao hoa và đặt đơn COD kèm Idempotency-Key.
- Kiểm tra tính idempotent (gửi lại cùng key phải trả về cùng đơn hàng, không trừ tồn 2 lần).
- Kiểm tra dữ liệu giữ kho (`HELD`) trong schema `inventory`.
- Hủy đơn hàng và kiểm tra việc giải phóng tồn kho tự động.
- Dọn dẹp sạch sẽ toàn bộ dữ liệu kiểm thử tổng hợp khỏi database.

---

## 9. Quyết định Nghiệp vụ cần xác nhận trước khi lên Production

1. **Chính sách hủy đơn khi hoa đã cắm**:
   - Hiện tại: Khách hàng chỉ được tự hủy đơn khi trạng thái là `PENDING` (chờ xử lý). Khi đơn đã sang `PROCESSING` (hoa đã được nghệ nhân cắt cành, cắm vào xốp/giỏ), hệ thống khóa quyền tự hủy của khách để tránh tổn thất nguyên vật liệu. Cần xác nhận tỷ lệ hoàn tiền hoặc phí hủy nếu khách yêu cầu hủy qua tổng đài khi hoa đã cắm xong.
2. **Thời hạn giữ tồn kho (Stock Reservation TTL)**:
   - Hiện tại đơn `PENDING` được giữ tồn cho tới khi xác nhận hoặc hủy. Trên production nên bổ sung một cron job/worker tự động nhả tồn sau `15 - 30 phút` nếu khách hàng không hoàn tất thanh toán trực tuyến.
3. **Chính sách thay thế hoa theo mùa**:
   - Cần bổ sung quy trình chụp ảnh thực tế gửi khách hàng qua Zalo/Email xác nhận trước khi giao nếu có loại hoa phụ buộc phải thay thế do khan hiếm theo thời tiết.

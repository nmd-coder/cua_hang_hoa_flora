import type { Metadata } from "next";
import { cookies } from "next/headers";
import "./style.css";

export const metadata: Metadata = {
  title: "FLORA | Hoa tươi cho những dịp đặc biệt",
  description: "Cửa hàng hoa trực tuyến FLORA - Giao hoa hỏa tốc trong 2 giờ, tuyển chọn 100% hoa tươi loại 1",
};

import { getGatewayUrl } from "../lib/config";

const getBase = () => getGatewayUrl();

export default async function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const token = (await cookies()).get("flora_session")?.value;
  let userProfile: { email?: string; roles?: string[] } | null = null;
  let cartCount = 0;

  if (token) {
    try {
      const meRes = await fetch(getBase() + "/api/auth/me", {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
        signal: AbortSignal.timeout(4000),
      });
      if (meRes.ok) userProfile = await meRes.json();

      const cartRes = await fetch(getBase() + "/api/cart", {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
        signal: AbortSignal.timeout(4000),
      });
      if (cartRes.ok) {
        const cartItems = await cartRes.json() as { quantity: number }[];
        cartCount = cartItems.reduce((acc, curr) => acc + (curr.quantity || 1), 0);
      }
    } catch {
      // Continue gracefully if network fails
    }
  }

  const isAdmin = userProfile?.roles?.includes("ADMIN");
  const isStaff = userProfile?.roles?.includes("STAFF") || isAdmin;

  return (
    <html lang="vi">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=Be+Vietnam+Pro:wght@300;400;500;600;700;800&display=swap"
          rel="stylesheet"
        />
      </head>
      <body>
        <header className="site-header">
          {/* Logo & Tagline */}
          <div className="header-brand">
            <a href="/" className="header-logo" title="FLORA - Trang chủ">
              FLORA<span className="logo-dot">.</span>
            </a>
            <span className="header-tagline">· Hoa tươi nghệ thuật & Quà tặng</span>
          </div>

          {/* Điều hướng chính: Trang chủ, Danh mục hoa */}
          <div className="header-nav-main">
            <a href="/" className="nav-link">
              Trang chủ
            </a>

            <div className="nav-dropdown">
              <a href="/san-pham" className="nav-link nav-dropdown-toggle">
                Danh mục hoa <span className="dropdown-arrow">▾</span>
              </a>
              <div className="nav-dropdown-menu">
                <a href="/san-pham?category=hoa-bo" className="dropdown-item">
                  <span className="dropdown-icon">💐</span>
                  <div className="dropdown-text">
                    <strong>Hoa bó cao cấp</strong>
                    <small>Thiết kế sang trọng, tinh tế</small>
                  </div>
                </a>
                <a href="/san-pham?category=gio-hoa" className="dropdown-item">
                  <span className="dropdown-icon">🧺</span>
                  <div className="dropdown-text">
                    <strong>Giỏ hoa tươi</strong>
                    <small>Thanh lịch cho mọi không gian</small>
                  </div>
                </a>
                <a href="/san-pham?category=ke-hoa-khai-truong" className="dropdown-item">
                  <span className="dropdown-icon">🎉</span>
                  <div className="dropdown-text">
                    <strong>Kệ hoa khai trương</strong>
                    <small>Hồng phát, thịnh vượng</small>
                  </div>
                </a>
                <a href="/san-pham?category=lan-ho-diep" className="dropdown-item">
                  <span className="dropdown-icon">🪴</span>
                  <div className="dropdown-text">
                    <strong>Lan hồ điệp</strong>
                    <small>Quý phái, sang trọng lâu tàn</small>
                  </div>
                </a>
                <a href="/san-pham?category=hop-hoa-nghe-thuat" className="dropdown-item">
                  <span className="dropdown-icon">🎁</span>
                  <div className="dropdown-text">
                    <strong>Hộp hoa nghệ thuật</strong>
                    <small>Bất ngờ và đầy ấn tượng</small>
                  </div>
                </a>
                <div className="dropdown-divider"></div>
                <a href="/san-pham" className="dropdown-item dropdown-item-all">
                  <span>✨ <strong>Xem tất cả sản phẩm hoa</strong></span>
                  <span>→</span>
                </a>
              </div>
            </div>
          </div>

          {/* Khung tìm kiếm */}
          <form action="/san-pham" method="GET" className="header-search">
            <span className="search-icon" aria-hidden="true">🔍</span>
            <input
              name="q"
              placeholder="Tìm hoa tươi, hoa sinh nhật, tulip, hoa bó..."
              aria-label="Tìm kiếm hoa"
            />
          </form>

          {/* Nhóm thao tác: Giỏ hàng, Đơn hàng, Nhân viên/Quản trị, Đăng nhập, Đăng ký */}
          <nav className="header-actions" aria-label="Điều hướng người dùng">
            <a href="/gio-hang" className="nav-action-link" title="Xem giỏ hàng">
              <span className="action-icon">🛒</span>
              <span className="action-text">Giỏ hàng</span>
              {cartCount > 0 && <span className="badge-count">{cartCount}</span>}
            </a>

            <a href="/don-hang" className="nav-action-link" title="Theo dõi đơn hàng">
              <span className="action-icon">📦</span>
              <span className="action-text">Đơn hàng</span>
            </a>

            {isStaff && (
              <a href="/nhan-vien" className="nav-link" style={{ color: "#3476C2", fontWeight: 700 }} title="Cổng nhân viên">
                Nhân viên
              </a>
            )}

            {isAdmin && (
              <a href="/admin" className="nav-link" style={{ color: "#C28E34", fontWeight: 700 }} title="Cổng quản trị">
                Quản trị
              </a>
            )}

            {userProfile ? (
              <a href="/tai-khoan" className="button button-sm button-secondary" title="Hồ sơ tài khoản">
                👤 {userProfile.email?.split("@")[0] || "Tài khoản"}
              </a>
            ) : (
              <div className="auth-buttons">
                <a href="/tai-khoan#dang-nhap" className="button button-sm auth-btn-login">
                  Đăng nhập
                </a>
                <a href="/tai-khoan#dang-ky" className="button button-sm auth-btn-register">
                  Đăng ký
                </a>
              </div>
            )}
          </nav>
        </header>

        <main className="site-main">{children}</main>

        <footer className="site-footer" id="chinh-sach">
          <div className="footer-grid">
            <div className="footer-col">
              <h4>CỬA HÀNG HOA TƯƠI FLORA</h4>
              <p style={{ color: "#A7B3AB", fontSize: "0.9rem" }}>
                Mang hương sắc thiên nhiên và lời nhắn gửi chân thành đến người bạn yêu thương.
              </p>
              <p style={{ color: "#A7B3AB", fontSize: "0.85rem", marginTop: "0.6rem" }}>
                Hotline: 1900 6868 (07:00 - 21:00)<br />
                Địa chỉ: 123 Lê Lợi, Phường Bến Nghé, Quận 1, TP. HCM<br />
                Email: lienhe@flora.vn
              </p>
            </div>

            <div className="footer-col">
              <h4>DANH MỤC HOA</h4>
              <a href="/san-pham?category=hoa-bo">Hoa bó cao cấp</a>
              <a href="/san-pham?category=gio-hoa">Giỏ hoa tươi sang trọng</a>
              <a href="/san-pham?category=ke-hoa-khai-truong">Kệ hoa khai trương</a>
              <a href="/san-pham?category=lan-ho-diep">Chậu lan hồ điệp</a>
              <a href="/san-pham?category=hop-hoa-nghe-thuat">Hộp hoa sáp & nghệ thuật</a>
            </div>

            <div className="footer-col">
              <h4>DỊP TẶNG HOA</h4>
              <a href="/san-pham?occasion=sinh-nhat">Hoa sinh nhật</a>
              <a href="/san-pham?occasion=tinh-yeu-ky-niem">Tình yêu & Kỷ niệm</a>
              <a href="/san-pham?occasion=khai-truong">Khai trương hồng phát</a>
              <a href="/san-pham?occasion=chuc-mung">Chúc mừng & Tốt nghiệp</a>
              <a href="/san-pham?occasion=chia-buon">Hoa chia buồn</a>
            </div>

            <div className="footer-col">
              <h4>CAM KẾT & CHÍNH SÁCH</h4>
              <a href="#chinh-sach">Giao hoa hỏa tốc 2 giờ</a>
              <a href="#chinh-sach">Hoa tươi loại 1 tuyển chọn 100%</a>
              <a href="#chinh-sach">Tặng kèm thiệp & banner cao cấp</a>
              <a href="#chinh-sach">Chính sách bảo hành & đổi trả</a>
            </div>
          </div>

          <div className="footer-bottom">
            © 2026 FLORA - Hệ thống bán hoa trực tuyến Microservices. Tất cả các quyền được bảo lưu.
          </div>
        </footer>
      </body>
    </html>
  );
}

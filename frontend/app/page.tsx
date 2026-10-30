import { ProductCard } from "../components/ProductCard";
import { products, getCategories, getOccasions, type Category, type Occasion } from "../lib/catalog";

export default async function Home({ searchParams }: { searchParams: Promise<{ q?: string; error?: string; notice?: string }> }) {
  const params = await searchParams;
  const q = params.q || "";

  let items = null;
  let categories: Category[] = [];
  let occasions: Occasion[] = [];

  try {
    [items, categories, occasions] = await Promise.all([
      products(q),
      getCategories(),
      getOccasions(),
    ]);
  } catch {
    /* Handle error gracefully */
  }

  return (
    <>
      {params.error && <p role="alert">{params.error}</p>}
      {params.notice && <p role="status">{params.notice}</p>}

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">
            <span className="hero-badge-dot"></span>
            <span>TIỆM HOA NGHỆ THUẬT & QUÀ TẶNG CAO CẤP</span>
          </div>
          <h1>Gửi một chút dịu dàng, trọn vẹn yêu thương.</h1>
          <p className="hero-lead">
            Mỗi bó hoa là một câu chuyện được kết tinh từ sắc màu và hương thơm thuần khiết, thiết kế thủ công và giao hỏa tốc tận tay người nhận trong 2 giờ.
          </p>
          <div className="hero-cta-group">
            <a className="button button-primary-lg" href="/san-pham">
              Khám phá ngay <span className="arrow-icon">→</span>
            </a>
            <a className="button button-secondary button-secondary-lg" href="#tim-hoa-nhanh">
              Tìm hoa theo dịp
            </a>
          </div>

          <div className="hero-trust-list">
            <div className="trust-item">
              <span className="trust-check">✓</span> Giao hỏa tốc 2 giờ
            </div>
            <div className="trust-item">
              <span className="trust-check">✓</span> 100% Hoa tươi loại 1
            </div>
            <div className="trust-item">
              <span className="trust-check">✓</span> Tặng thiệp thiết kế riêng
            </div>
          </div>
        </div>

        <div className="hero-visual">
          <div className="hero-image-card">
            <img
              src="https://images.unsplash.com/photo-1561181286-d3fee7d55364?auto=format&fit=crop&w=700&q=85"
              alt="Bó hoa tươi nghệ thuật FLORA"
              className="hero-flower-circle"
            />
            <div className="floating-chip floating-chip-top">
              <span className="chip-icon">⚡</span>
              <div>
                <strong>Giao siêu tốc 2H</strong>
                <p>Nội thành TP.HCM & Hà Nội</p>
              </div>
            </div>
            <div className="floating-chip floating-chip-bottom">
              <span className="chip-icon">⭐</span>
              <div>
                <strong>Đánh giá 4.9/5</strong>
                <p>Từ 10.000+ khách hàng</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Quick Flower Finder */}
      <section id="tim-hoa-nhanh" className="finder-card">
        <div className="finder-header">
          <p className="eyebrow" style={{ textAlign: "center", marginBottom: "0.2rem" }}>TÌM KIẾM NHANH CHÓNG</p>
          <h2 style={{ textAlign: "center", fontSize: "1.45rem", marginBottom: "1.2rem", fontWeight: 700 }}>Tìm bó hoa hoàn hảo cho dịp của bạn</h2>
        </div>
        <form action="/san-pham" method="GET" className="finder-grid">
          <div className="finder-field">
            <label htmlFor="finder-occasion">🎁 Dịp tặng</label>
            <select id="finder-occasion" name="occasion">
              <option value="">Tất cả các dịp</option>
              {occasions.map(o => (
                <option key={o.id} value={o.slug}>{o.name}</option>
              ))}
            </select>
          </div>

          <div className="finder-field">
            <label htmlFor="finder-category">💐 Loại thiết kế</label>
            <select id="finder-category" name="category">
              <option value="">Tất cả thiết kế</option>
              {categories.map(c => (
                <option key={c.id} value={c.slug}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="finder-field">
            <label htmlFor="finder-budget">💰 Ngân sách</label>
            <select id="finder-budget" name="budget">
              <option value="">Tất cả mức giá</option>
              <option value="under500">Dưới 500.000đ</option>
              <option value="500to1000">500.000đ - 1.000.000đ</option>
              <option value="over1000">Trên 1.000.000đ</option>
            </select>
          </div>

          <div className="finder-action">
            <button style={{ width: "100%", height: "46px" }}>Tìm hoa</button>
          </div>
        </form>
      </section>

      {/* Featured Categories */}
      <section style={{ marginBottom: "3.5rem" }}>
        <div style={{ textAlign: "center", marginBottom: "1.8rem" }}>
          <p className="eyebrow">BỘ SƯU TẬP HOA TƯƠI</p>
          <h2>Danh mục nổi bật</h2>
        </div>
        <div className="categories-grid">
          <a href="/san-pham?category=hoa-bo" className="category-pill">
            <span className="icon">💐</span>
            <span className="name">Hoa bó cao cấp</span>
          </a>
          <a href="/san-pham?category=gio-hoa" className="category-pill">
            <span className="icon">🧺</span>
            <span className="name">Giỏ hoa tươi</span>
          </a>
          <a href="/san-pham?category=hop-hoa-nghe-thuat" className="category-pill">
            <span className="icon">🎁</span>
            <span className="name">Hộp hoa nghệ thuật</span>
          </a>
          <a href="/san-pham?category=ke-hoa-khai-truong" className="category-pill">
            <span className="icon">🎊</span>
            <span className="name">Kệ khai trương</span>
          </a>
          <a href="/san-pham?category=lan-ho-diep" className="category-pill">
            <span className="icon">🌸</span>
            <span className="name">Lan hồ điệp</span>
          </a>
        </div>
      </section>

      {/* Featured Products */}
      <section id="danh-muc" style={{ marginBottom: "4rem" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.8rem" }}>
          <div>
            <p className="eyebrow">DÀNH CHO BẠN</p>
            <h2>Hoa tươi được yêu thích nhất</h2>
          </div>
          <a href="/san-pham" className="button button-secondary">
            Xem tất cả ({items ? items.length : "..."}) →
          </a>
        </div>

        {items === null ? (
          <p role="alert">Không thể tải danh mục lúc này. Vui lòng thử lại sau.</p>
        ) : items.length === 0 ? (
          <p>Không tìm thấy sản phẩm phù hợp.</p>
        ) : (
          <div className="product-grid">
            {items.map(p => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>

      {/* Store Promises / Value Props */}
      <section className="value-props">
        <div className="value-prop-card">
          <span className="icon">⚡</span>
          <h3>Giao hỏa tốc 2 giờ</h3>
          <p>Cam kết giao hoa tươi mới đúng hẹn tận nơi tại TP. Hồ Chí Minh và khu vực lân cận.</p>
        </div>
        <div className="value-prop-card">
          <span className="icon">🌿</span>
          <h3>100% Hoa tươi loại 1</h3>
          <p>Hoa được chọn lọc kỹ càng từ các nhà vườn Đà Lạt và nhập khẩu chất lượng cao mỗi sáng.</p>
        </div>
        <div className="value-prop-card">
          <span className="icon">💌</span>
          <h3>Miễn phí thiệp & banner</h3>
          <p>Tặng kèm thiệp chúc mừng thiết kế tinh tế với lời nhắn gửi trao ấm áp của bạn.</p>
        </div>
        <div className="value-prop-card">
          <span className="icon">🛡️</span>
          <h3>Bảo hành đổi trả</h3>
          <p>Chụp hình sản phẩm trước khi giao; cam kết đổi mới nếu hoa dập nát hoặc không đúng mẫu.</p>
        </div>
      </section>

      {/* Customer Reviews Section */}
      <section style={{ marginBottom: "3rem" }}>
        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <p className="eyebrow">KHÁCH HÀNG NÓI VỀ FLORA</p>
          <h2>Những trải nghiệm ngọt ngào</h2>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: "1.5rem" }}>
          <div className="panel" style={{ margin: 0 }}>
            <div style={{ color: "#F59E0B", marginBottom: "0.5rem" }}>★★★★★</div>
            <p style={{ fontStyle: "italic", fontSize: "0.95rem" }}>
              "Bó hoa hồng đỏ Ohara rất thơm và tươi, giao đúng 9h sáng ngay ngày kỷ niệm của hai đứa. Florist viết thiệp tay rất đẹp!"
            </p>
            <div style={{ fontWeight: 600, fontSize: "0.85rem", marginTop: "0.8rem", color: "var(--primary)" }}>
              Nguyễn Thị Ánh Dương · Quận 1
            </div>
          </div>

          <div className="panel" style={{ margin: 0 }}>
            <div style={{ color: "#F59E0B", marginBottom: "0.5rem" }}>★★★★★</div>
            <p style={{ fontStyle: "italic", fontSize: "0.95rem" }}>
              "Kệ hoa khai trương 2 tầng cực kỳ hoành tráng và rực rỡ, đối tác nhận được rất ưng ý. Nhân viên hỗ trợ nhiệt tình và chụp ảnh xác nhận trước khi giao."
            </p>
            <div style={{ fontWeight: 600, fontSize: "0.85rem", marginTop: "0.8rem", color: "var(--primary)" }}>
              Hoàng Minh Tuấn · TP. Thủ Đức
            </div>
          </div>

          <div className="panel" style={{ margin: 0 }}>
            <div style={{ color: "#F59E0B", marginBottom: "0.5rem" }}>★★★★★</div>
            <p style={{ fontStyle: "italic", fontSize: "0.95rem" }}>
              "Bó hoa hướng dương Rạng Rỡ Ánh Dương phối hoa baby bồng bềnh chụp hình tốt nghiệp sáng bừng luôn. Giá cả lại rất hợp lý."
            </p>
            <div style={{ fontWeight: 600, fontSize: "0.85rem", marginTop: "0.8rem", color: "var(--primary)" }}>
              Lê Thảo My · Quận 3
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

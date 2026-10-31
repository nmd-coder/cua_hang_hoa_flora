import { cookies } from "next/headers";
import { price, productDetail, type Product } from "../../lib/catalog";
import { addToCart, removeFromWishlist } from "../actions";
import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();

export default async function WishlistPage({
  searchParams,
}: {
  searchParams: Promise<{ notice?: string; error?: string }>;
}) {
  const query = await searchParams;
  const token = (await cookies()).get("flora_session")?.value;

  let wishlistIds: string[] = [];
  let products: Product[] = [];

  if (token) {
    try {
      const res = await fetch(getBase() + "/api/customer/wishlist", {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
        signal: AbortSignal.timeout(5000),
      });
      if (res.ok) {
        wishlistIds = (await res.json()) as string[];
      }
    } catch {}

    if (wishlistIds.length > 0) {
      const details = await Promise.all(
        wishlistIds.map(async id => {
          try {
            return await productDetail(id);
          } catch {
            return null;
          }
        })
      );
      products = details.filter((p): p is Product => p !== null);
    }
  }

  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto", padding: "1.5rem 1rem 4rem" }}>
      {/* Breadcrumb / Eyebrow */}
      <div style={{ marginBottom: "1rem" }}>
        <p className="eyebrow">BỘ SƯU TẬP CỦA BẠN</p>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", flexWrap: "wrap", gap: "0.8rem" }}>
          <h1 style={{ margin: 0 }}>Danh sách hoa yêu thích</h1>
          {token && products.length > 0 && (
            <span style={{ color: "var(--text-muted)", fontSize: "0.95rem" }}>
              Đang lưu <strong>{products.length}</strong> mẫu hoa
            </span>
          )}
        </div>
      </div>

      {query.error && <p role="alert">{query.error}</p>}
      {query.notice && <p role="status">{query.notice}</p>}

      {!token ? (
        <div className="panel" style={{ textAlign: "center", padding: "3.5rem 1.5rem", maxWidth: "600px", margin: "2rem auto" }}>
          <span style={{ fontSize: "3.5rem", display: "block", marginBottom: "1rem" }}>💖</span>
          <h2>Lưu giữ những mẫu hoa bạn yêu thích</h2>
          <p style={{ color: "var(--text-muted)", margin: "0.8rem 0 1.8rem", lineHeight: 1.6 }}>
            Đăng nhập để xem danh sách các mẫu hoa bạn đã lưu, dễ dàng đặt hoa bất cứ lúc nào hoặc nhận thông báo khi có ưu đãi đặc biệt.
          </p>
          <div style={{ display: "flex", justifyContent: "center", gap: "0.8rem", flexWrap: "wrap" }}>
            <a href="/dang-nhap?notice=Vui%20l%C3%B2ng%20%C4%91%C4%83ng%20nh%E1%BA%ADp%20%C4%91%E1%BB%83%20xem%20danh%20s%C3%A1ch%20y%C3%AAu%20th%C3%ADch" className="button">
              Đăng nhập ngay
            </a>
            <a href="/dang-ky" className="button button-secondary">
              Tạo tài khoản mới
            </a>
          </div>
        </div>
      ) : products.length === 0 ? (
        <div className="panel" style={{ textAlign: "center", padding: "4rem 1.5rem", maxWidth: "650px", margin: "2rem auto" }}>
          <span style={{ fontSize: "3.5rem", display: "block", marginBottom: "1rem" }}>🌸</span>
          <h2>Danh sách yêu thích đang trống</h2>
          <p style={{ color: "var(--text-muted)", margin: "0.8rem 0 2rem", lineHeight: 1.6 }}>
            Bạn chưa lưu mẫu hoa nào vào danh sách yêu thích. Hãy khám phá các bó hoa, giỏ hoa xinh xắn và nhấn biểu tượng <strong>"♡ Thêm vào danh sách yêu thích"</strong> để lưu lại nhé!
          </p>
          <a href="/san-pham" className="button" style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem" }}>
            <span>Khám phá bộ sưu tập hoa</span>
            <span>→</span>
          </a>
        </div>
      ) : (
        <div>
          <div className="product-grid" style={{ marginTop: "1.5rem" }}>
            {products.map(p => {
              const detailUrl = "/san-pham/" + (p.slug || p.id);
              return (
                <article key={p.id} className="flower-card">
                  <div className="flower-card-media-wrap">
                    <a href={detailUrl} className="flower-card-media" aria-label={`Xem chi tiết ${p.name}`}>
                      {p.image ? (
                        <img src={p.image} alt={p.name} loading="lazy" />
                      ) : (
                        <div className="flower-card-placeholder" aria-hidden="true">✿</div>
                      )}
                    </a>
                    <span className="card-badge-floating">♥ Đã yêu thích</span>
                  </div>

                  <div className="flower-card-body">
                    <div className="flower-card-meta-top">
                      {p.category_name && (
                        <span className="flower-card-category">{p.category_name}</span>
                      )}
                      <span className="card-stars">★★★★★</span>
                    </div>

                    <h3 className="flower-card-title">
                      <a href={detailUrl}>{p.name}</a>
                    </h3>

                    {p.description && (
                      <p className="flower-card-desc">{p.description}</p>
                    )}

                    <div className="flower-card-price-row">
                      <span className="price-label">Giá chỉ từ</span>
                      <div className="flower-card-price">{price(p.price)}</div>
                    </div>

                    <div className="flower-card-actions" style={{ flexDirection: "column", gap: "0.5rem" }}>
                      <div style={{ display: "flex", gap: "0.5rem", width: "100%" }}>
                        <form action={addToCart} style={{ flex: 1 }}>
                          <input type="hidden" name="id" value={p.id} />
                          <input type="hidden" name="quantity" value="1" />
                          <button type="submit" className="button button-add-cart" style={{ width: "100%" }}>
                            Thêm vào giỏ
                          </button>
                        </form>
                        <a href={detailUrl} className="button button-secondary button-detail" title="Xem chi tiết">
                          Chi tiết
                        </a>
                      </div>

                      {/* Remove from wishlist button */}
                      <form action={removeFromWishlist} style={{ width: "100%" }}>
                        <input type="hidden" name="productId" value={p.id} />
                        <button
                          type="submit"
                          className="button button-sm"
                          style={{
                            width: "100%",
                            background: "transparent",
                            border: "1px solid rgba(168, 50, 50, 0.25)",
                            color: "#A83232",
                            fontSize: "0.82rem",
                            padding: "0.35rem 0.6rem",
                          }}
                        >
                          ✕ Bỏ khỏi danh sách yêu thích
                        </button>
                      </form>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>

          <div style={{ marginTop: "3rem", textAlign: "center" }}>
            <a href="/san-pham" className="button button-secondary">
              ← Tiếp tục xem thêm mẫu hoa khác
            </a>
          </div>
        </div>
      )}
    </div>
  );
}


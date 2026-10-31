import { notFound } from "next/navigation";
import { cookies } from "next/headers";
import { productDetail, price, fetchCatalog, type Product } from "../../../lib/catalog";
import { addToCart, toggleWishlist } from "../../actions";
import { ProductCard } from "../../../components/ProductCard";

import { getGatewayUrl } from "../../../lib/config";

const getBase = () => getGatewayUrl();

export default async function ProductDetailPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ notice?: string; error?: string }>;
}) {
  const { id } = await params;
  const query = await searchParams;

  const product = await productDetail(id);
  if (!product) notFound();

  const token = (await cookies()).get("flora_session")?.value;
  let isFavorited = false;
  let reviews: {
    id: string;
    rating: number;
    comment: string;
    reviewer_name: string;
    created_at: string;
  }[] = [];

  // Fetch reviews from customer-service via gateway
  try {
    const revRes = await fetch(getBase() + "/api/reviews/product/" + product.id, {
      cache: "no-store",
      signal: AbortSignal.timeout(5000),
    });
    if (revRes.ok) reviews = await revRes.json();
  } catch {}

  // Check wishlist
  if (token) {
    try {
      const wishRes = await fetch(getBase() + "/api/customer/wishlist", {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
      });
      if (wishRes.ok) {
        const wishIds = await wishRes.json() as string[];
        isFavorited = wishIds.includes(product.id);
      }
    } catch {}
  }

  // Related products
  let relatedProducts: Product[] = [];
  try {
    const cat = product.category_slug || undefined;
    relatedProducts = (await fetchCatalog({ category: cat, size: 4 }))
      .filter(p => p.id !== product.id)
      .slice(0, 3);
  } catch {}

  const mainImage = product.image || (product.images && product.images[0]?.url);
  const avgRating = reviews.length > 0
    ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
    : "5.0";

  return (
    <div>
      {query.error && <p role="alert">{query.error}</p>}
      {query.notice && <p role="status">{query.notice}</p>}

      {/* Breadcrumb */}
      <nav style={{ fontSize: "0.85rem", color: "var(--text-muted)", marginBottom: "1.5rem" }} aria-label="Đường dẫn">
        <a href="/">Trang chủ</a> / <a href="/san-pham">Sản phẩm</a> /{" "}
        {product.category_name && (
          <>
            <a href={`/san-pham?category=${product.category_slug}`}>{product.category_name}</a> /{" "}
          </>
        )}
        <span style={{ color: "var(--text-main)" }}>{product.name}</span>
      </nav>

      <div className="detail-layout">
        {/* Gallery */}
        <div>
          <div className="gallery-main">
            {mainImage ? (
              <img src={mainImage} alt={product.name} />
            ) : (
              <div style={{ height: "420px", display: "grid", placeItems: "center", fontSize: "6rem", color: "var(--accent-pink)" }}>
                ✿
              </div>
            )}
          </div>

          {product.images && product.images.length > 1 && (
            <div className="gallery-thumbs">
              {product.images.map(img => (
                <div key={img.id} className="gallery-thumb">
                  <img src={img.url} alt={img.alt_text || product.name} />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Info & Buying */}
        <div className="detail-info">
          {product.category_name && (
            <p className="eyebrow">{product.category_name}</p>
          )}

          <h1>{product.name}</h1>
          <p className="detail-sku">Mã sản phẩm (SKU): <strong>{product.sku}</strong></p>

          <div style={{ display: "flex", alignItems: "center", gap: "0.8rem", marginBottom: "0.8rem" }}>
            <span style={{ color: "#F59E0B", fontWeight: 700 }}>★ {avgRating}</span>
            <span style={{ color: "var(--text-muted)", fontSize: "0.88rem" }}>({reviews.length} đánh giá)</span>
            <span className="stock-badge stock-in">Còn hàng</span>
          </div>

          <div className="detail-price">{price(product.price)}</div>

          {product.description && (
            <p style={{ lineHeight: 1.7, marginBottom: "1.2rem", color: "var(--text-main)" }}>
              {product.description}
            </p>
          )}

          {/* Specifications */}
          <div style={{ background: "#F5F2EB", borderRadius: "var(--radius-sm)", padding: "1rem", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
            {product.occasions && product.occasions.length > 0 && (
              <p style={{ marginBottom: "0.4rem" }}>
                <strong>Dịp phù hợp:</strong> {product.occasions.map(o => o.name).join(", ")}
              </p>
            )}
            {product.flower_types && product.flower_types.length > 0 && (
              <p style={{ marginBottom: "0.4rem" }}>
                <strong>Loại hoa chính:</strong> {product.flower_types.map(ft => ft.name).join(", ")}
              </p>
            )}
            {product.colors && product.colors.length > 0 && (
              <p style={{ marginBottom: "0.4rem" }}>
                <strong>Tông màu:</strong> {product.colors.map(c => c.name).join(", ")}
              </p>
            )}
            <p style={{ margin: 0 }}>
              <strong>Kích thước dự kiến:</strong> ~{product.length_cm || 30}cm × {product.width_cm || 25}cm (Cao {product.height_cm || 45}cm)
            </p>
          </div>

          {/* Action form */}
          <form action={addToCart} style={{ display: "flex", gap: "0.8rem", alignItems: "flex-end", marginBottom: "1rem" }}>
            <input type="hidden" name="id" value={product.id} />
            <div style={{ width: "90px" }}>
              <label htmlFor="qty">Số lượng</label>
              <input id="qty" type="number" name="quantity" defaultValue={1} min={1} max={99} required />
            </div>
            <button type="submit" style={{ flex: 1, height: "46px" }}>
              Thêm vào giỏ hàng
            </button>
          </form>

          {/* Wishlist toggle */}
          <form action={toggleWishlist} style={{ marginBottom: "0.5rem" }}>
            <input type="hidden" name="productId" value={product.id} />
            <input type="hidden" name="isFavorited" value={String(isFavorited)} />
            <input type="hidden" name="returnTo" value={`/san-pham/${product.slug}`} />
            <button
              type="submit"
              className="button button-secondary"
              style={{
                width: "100%",
                borderColor: isFavorited ? "var(--accent-peach)" : undefined,
                color: isFavorited ? "#A83232" : undefined,
                fontWeight: 600,
              }}
            >
              {isFavorited ? "♥ Đã lưu trong yêu thích (Bấm để hủy)" : "♡ Thêm vào danh sách yêu thích"}
            </button>
          </form>
          <div style={{ textAlign: "center", marginBottom: "1rem" }}>
            <a href="/yeu-thich" style={{ fontSize: "0.85rem", color: "var(--primary)", textDecoration: "underline", fontWeight: 600 }}>
              Xem danh sách yêu thích của bạn →
            </a>
          </div>

          {/* Store & Replacement Policy */}
          <div className="policy-box">
            <h4 style={{ fontSize: "0.95rem", marginBottom: "0.4rem", color: "var(--primary)" }}>
              Chính sách hoa tươi & thay thế hoa:
            </h4>
            <p style={{ fontSize: "0.86rem", color: "var(--text-muted)", margin: 0 }}>
              Do đặc thù nguồn hoa tươi nở theo mùa vụ tự nhiên, trường hợp một số loại hoa phụ hoặc giấy gói hết hàng trong ngày, Florist của FLORA sẽ chủ động thay thế bằng loại hoa có giá trị tương đương hoặc cao hơn, đảm bảo phong cách và tính thẩm mỹ cao nhất cho bó hoa của bạn.
            </p>
          </div>
        </div>
      </div>

      {/* Customer Reviews Section */}
      <section style={{ marginTop: "3rem", paddingTop: "2rem", borderTop: "1px solid var(--border)" }}>
        <h2>Đánh giá từ khách hàng ({reviews.length})</h2>

        {reviews.length === 0 ? (
          <p style={{ color: "var(--text-muted)", margin: "1rem 0" }}>
            Chưa có đánh giá nào cho sản phẩm này. Khách hàng đã mua và nhận hoa có thể để lại đánh giá trong mục Đơn hàng.
          </p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginTop: "1.2rem" }}>
            {reviews.map(rev => (
              <article key={rev.id} className="panel" style={{ margin: 0 }}>
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.4rem" }}>
                  <strong>{rev.reviewer_name}</strong>
                  <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                    {new Date(rev.created_at).toLocaleDateString("vi-VN")}
                  </span>
                </div>
                <div style={{ color: "#F59E0B", marginBottom: "0.4rem" }}>
                  {"★".repeat(rev.rating)}{"☆".repeat(5 - rev.rating)}
                </div>
                <p style={{ margin: 0, fontSize: "0.92rem" }}>{rev.comment}</p>
              </article>
            ))}
          </div>
        )}
      </section>

      {/* Related Products */}
      {relatedProducts.length > 0 && (
        <section style={{ marginTop: "4rem" }}>
          <h2>Sản phẩm liên quan</h2>
          <div className="product-grid" style={{ marginTop: "1.5rem" }}>
            {relatedProducts.map(p => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

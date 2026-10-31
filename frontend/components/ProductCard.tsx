import type { Product } from "../lib/catalog";
import { price } from "../lib/catalog";
import { addToCart } from "../app/actions";

export function ProductCard({ product }: { product: Product }) {
  const detailUrl = "/san-pham/" + (product.slug || product.id);
  const hasDiscount = Boolean(product.original_price && product.original_price > product.price);
  const discountPercent = hasDiscount
    ? Math.round((1 - product.price / product.original_price!) * 100)
    : 0;

  return (
    <article className="flower-card">
      <div className="flower-card-media-wrap">
        <a href={detailUrl} className="flower-card-media" aria-label={`Xem chi tiết ${product.name}`}>
          {product.image ? (
            <img
              src={product.image}
              alt={product.name}
              loading="lazy"
            />
          ) : (
            <div className="flower-card-placeholder" aria-hidden="true">✿</div>
          )}
        </a>
        <span className="card-badge-floating">⚡ Giao 2h</span>
        {hasDiscount && (
          <span className="card-badge-discount">-{discountPercent}%</span>
        )}
      </div>

      <div className="flower-card-body">
        <div className="flower-card-meta-top">
          {product.category_name && (
            <span className="flower-card-category">{product.category_name}</span>
          )}
          <span className="card-stars">★★★★★</span>
        </div>

        <h3 className="flower-card-title">
          <a href={detailUrl}>{product.name}</a>
        </h3>

        {product.description && (
          <p className="flower-card-desc">
            {product.description}
          </p>
        )}

        <div className="flower-card-price-row">
          <span className="price-label">Giá ưu đãi</span>
          <div className="flower-card-price-cluster">
            <span className="flower-card-price">{price(product.price)}</span>
            {hasDiscount && (
              <span className="flower-card-original-price">{price(product.original_price!)}</span>
            )}
          </div>
        </div>

        <div className="flower-card-actions">
          <form action={addToCart} style={{ flex: 1 }}>
            <input type="hidden" name="id" value={product.id} />
            <input type="hidden" name="quantity" value="1" />
            <button type="submit" className="button button-add-cart" style={{ width: "100%" }}>
              Thêm vào giỏ
            </button>
          </form>
          <a href={detailUrl} className="button button-secondary button-detail" title="Xem chi tiết">
            Chi tiết
          </a>
        </div>
      </div>
    </article>
  );
}

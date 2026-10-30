import { ProductCard } from "../../components/ProductCard";
import {
  fetchCatalog,
  getCategories,
  getOccasions,
  getFlowerTypes,
  getColors,
  type Category,
  type Occasion,
  type FlowerType,
  type Color,
} from "../../lib/catalog";

export default async function ProductCatalog({
  searchParams,
}: {
  searchParams: Promise<{
    q?: string;
    category?: string;
    occasion?: string;
    flower_type?: string;
    color?: string;
    budget?: string;
    min_price?: string;
    max_price?: string;
    sort?: string;
    error?: string;
    notice?: string;
  }>;
}) {
  const params = await searchParams;
  const q = params.q || "";
  const category = params.category || "";
  const occasion = params.occasion || "";
  const flowerType = params.flower_type || "";
  const color = params.color || "";
  const sort = params.sort || "newest";

  let minPrice: number | undefined = params.min_price ? Number(params.min_price) : undefined;
  let maxPrice: number | undefined = params.max_price ? Number(params.max_price) : undefined;

  // Budget preset mapping
  if (params.budget === "under500") {
    maxPrice = 500000;
  } else if (params.budget === "500to1000") {
    minPrice = 500000;
    maxPrice = 1000000;
  } else if (params.budget === "over1000") {
    minPrice = 1000000;
  }

  let items = null;
  let categories: Category[] = [];
  let occasions: Occasion[] = [];
  let flowerTypes: FlowerType[] = [];
  let colors: Color[] = [];

  try {
    [items, categories, occasions, flowerTypes, colors] = await Promise.all([
      fetchCatalog({
        q,
        category,
        occasion,
        flowerType,
        color,
        minPrice,
        maxPrice,
        sort,
        size: 50,
      }),
      getCategories(),
      getOccasions(),
      getFlowerTypes(),
      getColors(),
    ]);
  } catch {
    /* Handle error */
  }

  return (
    <div>
      <div style={{ marginBottom: "2rem" }}>
        <p className="eyebrow">DANH MỤC HOA TƯƠI</p>
        <h1>Tất cả sản phẩm hoa</h1>
        <p style={{ color: "var(--text-muted)" }}>
          Tuyển chọn những đóa hoa tươi rạng rỡ nhất, sẵn sàng trao gửi những thông điệp yêu thương.
        </p>
      </div>

      {params.error && <p role="alert">{params.error}</p>}
      {params.notice && <p role="status">{params.notice}</p>}

      <div className="catalog-layout">
        {/* Filter Sidebar */}
        <aside className="filter-sidebar">
          <form method="GET" action="/san-pham">
            <h3 style={{ marginBottom: "1rem", fontSize: "1.1rem" }}>Bộ lọc tìm kiếm</h3>

            {/* Keyword */}
            <div className="filter-group">
              <label htmlFor="filter-q" className="filter-title">Từ khóa</label>
              <input
                id="filter-q"
                name="q"
                defaultValue={q}
                placeholder="Tên hoặc mô tả hoa..."
              />
            </div>

            {/* Category */}
            <div className="filter-group">
              <div className="filter-title">Danh mục</div>
              <div className="filter-options">
                <label>
                  <input type="radio" name="category" value="" defaultChecked={!category} />
                  Tất cả
                </label>
                {categories.map(c => (
                  <label key={c.id}>
                    <input type="radio" name="category" value={c.slug} defaultChecked={category === c.slug} />
                    {c.name}
                  </label>
                ))}
              </div>
            </div>

            {/* Occasion */}
            <div className="filter-group">
              <div className="filter-title">Dịp tặng hoa</div>
              <div className="filter-options">
                <label>
                  <input type="radio" name="occasion" value="" defaultChecked={!occasion} />
                  Tất cả các dịp
                </label>
                {occasions.map(o => (
                  <label key={o.id}>
                    <input type="radio" name="occasion" value={o.slug} defaultChecked={occasion === o.slug} />
                    {o.name}
                  </label>
                ))}
              </div>
            </div>

            {/* Flower Type */}
            <div className="filter-group">
              <div className="filter-title">Loại hoa chủ đạo</div>
              <div className="filter-options">
                <label>
                  <input type="radio" name="flower_type" value="" defaultChecked={!flowerType} />
                  Tất cả
                </label>
                {flowerTypes.map(ft => (
                  <label key={ft.id}>
                    <input type="radio" name="flower_type" value={ft.slug} defaultChecked={flowerType === ft.slug} />
                    {ft.name}
                  </label>
                ))}
              </div>
            </div>

            {/* Color */}
            <div className="filter-group">
              <div className="filter-title">Màu sắc chủ đạo</div>
              <div className="filter-options">
                <label>
                  <input type="radio" name="color" value="" defaultChecked={!color} />
                  Tất cả
                </label>
                {colors.map(col => (
                  <label key={col.id} style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                    <input type="radio" name="color" value={col.name} defaultChecked={color === col.name} />
                    <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: col.hex_code || "#ccc", display: "inline-block", border: "1px solid #ccc" }}></span>
                    {col.name}
                  </label>
                ))}
              </div>
            </div>

            {/* Budget Presets */}
            <div className="filter-group">
              <div className="filter-title">Mức giá</div>
              <div className="filter-options">
                <label>
                  <input type="radio" name="budget" value="" defaultChecked={!params.budget} />
                  Tất cả
                </label>
                <label>
                  <input type="radio" name="budget" value="under500" defaultChecked={params.budget === "under500"} />
                  Dưới 500.000đ
                </label>
                <label>
                  <input type="radio" name="budget" value="500to1000" defaultChecked={params.budget === "500to1000"} />
                  500.000đ - 1.000.000đ
                </label>
                <label>
                  <input type="radio" name="budget" value="over1000" defaultChecked={params.budget === "over1000"} />
                  Trên 1.000.000đ
                </label>
              </div>
            </div>

            <div style={{ display: "flex", flexDirection: "column", gap: "0.6rem", marginTop: "1rem" }}>
              <button type="submit" style={{ width: "100%" }}>Áp dụng bộ lọc</button>
              <a href="/san-pham" className="button button-secondary" style={{ width: "100%", textAlign: "center" }}>
                Xóa bộ lọc
              </a>
            </div>
          </form>
        </aside>

        {/* Products Grid */}
        <section>
          {/* Top Sort Bar */}
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", flexWrap: "wrap", gap: "1rem" }}>
            <p style={{ color: "var(--text-muted)", fontSize: "0.95rem", margin: 0 }}>
              Hiển thị <strong>{items ? items.length : 0}</strong> sản phẩm phù hợp
            </p>

            <form method="GET" action="/san-pham" style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              {q && <input type="hidden" name="q" value={q} />}
              {category && <input type="hidden" name="category" value={category} />}
              {occasion && <input type="hidden" name="occasion" value={occasion} />}
              {flowerType && <input type="hidden" name="flower_type" value={flowerType} />}
              {color && <input type="hidden" name="color" value={color} />}
              {params.budget && <input type="hidden" name="budget" value={params.budget} />}

              <label htmlFor="sort-select" style={{ marginBottom: 0, fontSize: "0.88rem", whiteSpace: "nowrap" }}>
                Sắp xếp:
              </label>
              <select id="sort-select" name="sort" defaultValue={sort} style={{ width: "auto", padding: "0.4rem 0.8rem" }}>
                <option value="newest">Mới nhất</option>
                <option value="price_asc">Giá: Thấp đến cao</option>
                <option value="price_desc">Giá: Cao đến thấp</option>
                <option value="name">Tên A-Z</option>
              </select>
              <button type="submit" className="button button-sm button-secondary">
                Lọc
              </button>
            </form>
          </div>

          {items === null ? (
            <p role="alert">Không thể kết nối đến máy chủ danh mục lúc này. Vui lòng kiểm tra lại dịch vụ.</p>
          ) : items.length === 0 ? (
            <div className="panel" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
              <span style={{ fontSize: "3rem", display: "block", marginBottom: "1rem" }}>🔍</span>
              <h3>Không tìm thấy sản phẩm phù hợp</h3>
              <p style={{ color: "var(--text-muted)", marginBottom: "1.5rem" }}>
                Hãy thử nới lỏng bộ lọc hoặc tìm kiếm bằng từ khóa khác.
              </p>
              <a href="/san-pham" className="button">Xem tất cả hoa</a>
            </div>
          ) : (
            <div className="product-grid">
              {items.map(p => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

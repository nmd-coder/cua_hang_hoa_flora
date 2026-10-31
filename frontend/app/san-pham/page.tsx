import { ProductCard } from "../../components/ProductCard";
import {
  fetchCatalog,
  getCategories,
  getOccasions,
  getFlowerTypes,
  getColors,
  price,
  type Category,
  type Occasion,
  type FlowerType,
  type Color,
  type Product,
} from "../../lib/catalog";

// Helper to construct filter URL without losing existing parameters
function buildFilterUrl(
  current: Record<string, string | number | undefined>,
  updates: Record<string, string | number | null | undefined>
): string {
  const merged: Record<string, string> = {};

  for (const [k, v] of Object.entries(current)) {
    if (v !== undefined && v !== "" && v !== null) {
      merged[k] = String(v);
    }
  }

  for (const [k, v] of Object.entries(updates)) {
    if (v === null || v === undefined || v === "") {
      delete merged[k];
    } else {
      merged[k] = String(v);
    }
  }

  const qs = new URLSearchParams(merged).toString();
  return qs ? `/san-pham?${qs}` : "/san-pham";
}

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
  const q = params.q?.trim() || "";
  const category = params.category?.trim() || "";
  const occasion = params.occasion?.trim() || "";
  const flowerType = params.flower_type?.trim() || "";
  const color = params.color?.trim() || "";
  const budget = params.budget?.trim() || "";
  const sort = params.sort?.trim() || "newest";

  let minPrice: number | undefined = params.min_price ? Number(params.min_price) : undefined;
  let maxPrice: number | undefined = params.max_price ? Number(params.max_price) : undefined;

  // Preset budget handling
  if (budget === "under500") {
    maxPrice = 500000;
    minPrice = undefined;
  } else if (budget === "500to1000") {
    minPrice = 500000;
    maxPrice = 1000000;
  } else if (budget === "1000to2000") {
    minPrice = 1000000;
    maxPrice = 2000000;
  } else if (budget === "over2000" || budget === "over1000") {
    minPrice = budget === "over2000" ? 2000000 : 1000000;
    maxPrice = undefined;
  }

  let items: Product[] | null = null;
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
    /* Handle error gracefully */
  }

  const currentParams: Record<string, string | number | undefined> = {
    q: q || undefined,
    category: category || undefined,
    occasion: occasion || undefined,
    flower_type: flowerType || undefined,
    color: color || undefined,
    budget: budget || undefined,
    min_price: params.min_price || undefined,
    max_price: params.max_price || undefined,
    sort: sort !== "newest" ? sort : undefined,
  };

  // Build active filter tags
  const activeTags: { key: string; label: string; removeUrl: string }[] = [];

  if (q) {
    activeTags.push({
      key: "q",
      label: `Từ khóa: "${q}"`,
      removeUrl: buildFilterUrl(currentParams, { q: null }),
    });
  }

  if (category) {
    const matched = categories.find((c) => c.slug === category);
    activeTags.push({
      key: "category",
      label: `Danh mục: ${matched ? matched.name : category}`,
      removeUrl: buildFilterUrl(currentParams, { category: null }),
    });
  }

  if (occasion) {
    const matched = occasions.find((o) => o.slug === occasion);
    activeTags.push({
      key: "occasion",
      label: `Dịp: ${matched ? matched.name : occasion}`,
      removeUrl: buildFilterUrl(currentParams, { occasion: null }),
    });
  }

  if (flowerType) {
    const matched = flowerTypes.find((f) => f.slug === flowerType);
    activeTags.push({
      key: "flower_type",
      label: `Giống hoa: ${matched ? matched.name : flowerType}`,
      removeUrl: buildFilterUrl(currentParams, { flower_type: null }),
    });
  }

  if (color) {
    activeTags.push({
      key: "color",
      label: `Màu sắc: ${color}`,
      removeUrl: buildFilterUrl(currentParams, { color: null }),
    });
  }

  if (budget) {
    let budgetLabel = "Mức giá";
    if (budget === "under500") budgetLabel = "Giá: Dưới 500.000đ";
    else if (budget === "500to1000") budgetLabel = "Giá: 500.000đ - 1.000.000đ";
    else if (budget === "1000to2000") budgetLabel = "Giá: 1.000.000đ - 2.000.000đ";
    else if (budget === "over2000") budgetLabel = "Giá: Trên 2.000.000đ";
    else if (budget === "over1000") budgetLabel = "Giá: Trên 1.000.000đ";

    activeTags.push({
      key: "budget",
      label: budgetLabel,
      removeUrl: buildFilterUrl(currentParams, { budget: null }),
    });
  } else if (params.min_price || params.max_price) {
    const minText = minPrice !== undefined ? price(minPrice) : "0đ";
    const maxText = maxPrice !== undefined ? price(maxPrice) : "trở lên";
    activeTags.push({
      key: "custom_price",
      label: `Giá: ${minText} - ${maxText}`,
      removeUrl: buildFilterUrl(currentParams, { min_price: null, max_price: null }),
    });
  }

  // Popular curated flower shortcuts
  const popularFlowers = [
    { slug: "hoa-hong", name: "Hoa hồng" },
    { slug: "hong-juliet", name: "Hồng Juliet" },
    { slug: "hong-pink-o-hara", name: "Pink O'Hara" },
    { slug: "mau-don-peony", name: "Mẫu đơn" },
    { slug: "hoa-tulip", name: "Tulip" },
    { slug: "cuc-tana", name: "Cúc Tana" },
    { slug: "hoa-sen", name: "Hoa Sen" },
    { slug: "protea", name: "King Protea" },
    { slug: "lan-ho-diep", name: "Lan hồ điệp" },
    { slug: "cam-tu-cau", name: "Cẩm tú cầu" },
    { slug: "hoa-baby", name: "Baby trắng" },
  ];

  return (
    <div>
      {/* Page Title & Breadcrumb Header */}
      <div style={{ marginBottom: "1.8rem" }}>
        <p className="eyebrow">BỘ SƯU TẬP HOA TƯƠI FLORA</p>
        <h1 style={{ marginBottom: "0.4rem" }}>Tất cả tác phẩm hoa</h1>
        <p style={{ color: "var(--text-muted)", fontSize: "1rem" }}>
          Tuyển chọn hơn 20 thiết kế nghệ thuật và 110+ giống hoa tươi cao cấp từ Đà Lạt và nhập khẩu.
        </p>
      </div>

      {params.error && <p role="alert">{params.error}</p>}
      {params.notice && <p role="status">{params.notice}</p>}

      {/* Modern Quick-Filter Ribbon (Scrollable horizontally) */}
      <nav aria-label="Bộ lọc nhanh" className="quick-filter-ribbon" style={{ marginBottom: "1.2rem" }}>
        <a
          href="/san-pham"
          className={`quick-pill ${
            !category && !flowerType && !budget && !color && !occasion && !q ? "active" : ""
          }`}
        >
          <span>🌿 Tất cả hoa</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { category: category === "hoa-bo" ? null : "hoa-bo" })}
          className={`quick-pill ${category === "hoa-bo" ? "active" : ""}`}
        >
          <span>💐 Hoa bó</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { category: category === "gio-hoa" ? null : "gio-hoa" })}
          className={`quick-pill ${category === "gio-hoa" ? "active" : ""}`}
        >
          <span>🧺 Giỏ hoa</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { category: category === "hop-hoa-nghe-thuat" ? null : "hop-hoa-nghe-thuat" })}
          className={`quick-pill ${category === "hop-hoa-nghe-thuat" ? "active" : ""}`}
        >
          <span>🎁 Hộp hoa</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { category: category === "ke-hoa-khai-truong" ? null : "ke-hoa-khai-truong" })}
          className={`quick-pill ${category === "ke-hoa-khai-truong" ? "active" : ""}`}
        >
          <span>🎉 Kệ khai trương</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { category: category === "lan-ho-diep" ? null : "lan-ho-diep" })}
          className={`quick-pill ${category === "lan-ho-diep" ? "active" : ""}`}
        >
          <span>🪴 Lan hồ điệp</span>
        </a>

        {/* Popular flower quick-pills */}
        <a
          href={buildFilterUrl(currentParams, { flower_type: flowerType === "hong-juliet" ? null : "hong-juliet" })}
          className={`quick-pill ${flowerType === "hong-juliet" ? "active" : ""}`}
        >
          <span>👑 Hồng Juliet</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { flower_type: flowerType === "mau-don-peony" ? null : "mau-don-peony" })}
          className={`quick-pill ${flowerType === "mau-don-peony" ? "active" : ""}`}
        >
          <span>🌸 Mẫu đơn</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { flower_type: flowerType === "hoa-tulip" ? null : "hoa-tulip" })}
          className={`quick-pill ${flowerType === "hoa-tulip" ? "active" : ""}`}
        >
          <span>🌷 Tulip</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { flower_type: flowerType === "cuc-tana" ? null : "cuc-tana" })}
          className={`quick-pill ${flowerType === "cuc-tana" ? "active" : ""}`}
        >
          <span>✨ Cúc Tana</span>
        </a>

        {/* Budget shortcuts */}
        <a
          href={buildFilterUrl(currentParams, { budget: budget === "under500" ? null : "under500", min_price: null, max_price: null })}
          className={`quick-pill ${budget === "under500" ? "active" : ""}`}
        >
          <span>⚡ Dưới 500k</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { budget: budget === "500to1000" ? null : "500to1000", min_price: null, max_price: null })}
          className={`quick-pill ${budget === "500to1000" ? "active" : ""}`}
        >
          <span>🌟 500k - 1tr</span>
        </a>
        <a
          href={buildFilterUrl(currentParams, { budget: budget === "over1000" ? null : "over1000", min_price: null, max_price: null })}
          className={`quick-pill ${budget === "over1000" ? "active" : ""}`}
        >
          <span>💎 Trên 1tr</span>
        </a>
      </nav>

      {/* Active Filters Bar (Shows when filters are selected) */}
      {activeTags.length > 0 && (
        <section aria-label="Bộ lọc đang chọn" className="active-filters-bar">
          <span className="active-filters-label">Đang lọc theo:</span>
          {activeTags.map((t) => (
            <a key={t.key} href={t.removeUrl} className="active-filter-chip" title="Bỏ lọc mục này">
              <span>{t.label}</span>
              <span className="chip-remove" aria-hidden="true">✕</span>
            </a>
          ))}
          <a href="/san-pham" className="clear-all-link">
            Xóa tất cả bộ lọc
          </a>
        </section>
      )}

      {/* Main Catalog Layout */}
      <div className="catalog-layout">
        {/* Modern Faceted Filter Sidebar */}
        <aside className="modern-filter-sidebar">
          {/* Keyword Search Box */}
          <form method="GET" action="/san-pham" className="sidebar-search-box">
            {category && <input type="hidden" name="category" value={category} />}
            {occasion && <input type="hidden" name="occasion" value={occasion} />}
            {flowerType && <input type="hidden" name="flower_type" value={flowerType} />}
            {color && <input type="hidden" name="color" value={color} />}
            {budget && <input type="hidden" name="budget" value={budget} />}
            {params.min_price && <input type="hidden" name="min_price" value={params.min_price} />}
            {params.max_price && <input type="hidden" name="max_price" value={params.max_price} />}
            {sort !== "newest" && <input type="hidden" name="sort" value={sort} />}

            <span className="sidebar-search-icon" aria-hidden="true">🔍</span>
            <input
              name="q"
              defaultValue={q}
              placeholder="Tìm theo tên hoa, ý nghĩa..."
              aria-label="Tìm kiếm sản phẩm"
            />
            {q && (
              <a
                href={buildFilterUrl(currentParams, { q: null })}
                className="sidebar-search-clear"
                title="Xóa từ khóa"
              >
                ✕
              </a>
            )}
          </form>

          {/* 1. Category Facet */}
          <div className="filter-group">
            <div className="filter-section-header">
              <span className="filter-section-title">Danh mục thiết kế</span>
              {category && (
                <a
                  href={buildFilterUrl(currentParams, { category: null })}
                  style={{ fontSize: "0.78rem", color: "var(--accent-peach)", textDecoration: "none" }}
                >
                  Bỏ chọn
                </a>
              )}
            </div>
            <div className="category-choice-list">
              <a
                href={buildFilterUrl(currentParams, { category: null })}
                className={`category-choice-item ${!category ? "active" : ""}`}
              >
                <span>Tất cả kiểu dáng</span>
                <span style={{ fontSize: "0.78rem", opacity: 0.7 }}>20</span>
              </a>
              {categories.map((c) => {
                const isActive = category === c.slug;
                return (
                  <a
                    key={c.id}
                    href={buildFilterUrl(currentParams, { category: isActive ? null : c.slug })}
                    className={`category-choice-item ${isActive ? "active" : ""}`}
                  >
                    <span>{c.name}</span>
                  </a>
                );
              })}
            </div>
          </div>

          {/* 2. Visual Color Palette Swatches */}
          <div className="filter-group">
            <div className="filter-section-header">
              <span className="filter-section-title">Bảng màu sắc</span>
              {color && (
                <a
                  href={buildFilterUrl(currentParams, { color: null })}
                  style={{ fontSize: "0.78rem", color: "var(--accent-peach)", textDecoration: "none" }}
                >
                  Bỏ chọn
                </a>
              )}
            </div>
            <div className="color-swatches-grid">
              {colors.map((col) => {
                const isActive = color === col.name;
                const isLight = col.name === "Trắng" || col.hex_code === "#F8F9FA";
                return (
                  <a
                    key={col.id}
                    href={buildFilterUrl(currentParams, { color: isActive ? null : col.name })}
                    className={`color-swatch-chip ${isActive ? "active" : ""}`}
                    title={`Lọc hoa tone màu ${col.name}`}
                  >
                    <span
                      className="color-circle"
                      style={{
                        background: col.hex_code || "#ccc",
                        color: isLight ? "#183D2D" : "#FFFFFF",
                        border: isLight ? "1px solid #D5D0C7" : "none",
                      }}
                    >
                      {isActive ? "✓" : ""}
                    </span>
                    <span className="color-chip-text">{col.name}</span>
                  </a>
                );
              })}
            </div>
          </div>

          {/* 3. Occasion Facet */}
          <div className="filter-group">
            <div className="filter-section-header">
              <span className="filter-section-title">Dịp trao tặng</span>
              {occasion && (
                <a
                  href={buildFilterUrl(currentParams, { occasion: null })}
                  style={{ fontSize: "0.78rem", color: "var(--accent-peach)", textDecoration: "none" }}
                >
                  Bỏ chọn
                </a>
              )}
            </div>
            <div className="filter-choice-pills">
              <a
                href={buildFilterUrl(currentParams, { occasion: null })}
                className={`filter-choice-pill ${!occasion ? "active" : ""}`}
              >
                Tất cả dịp
              </a>
              {occasions.map((o) => {
                const isActive = occasion === o.slug;
                return (
                  <a
                    key={o.id}
                    href={buildFilterUrl(currentParams, { occasion: isActive ? null : o.slug })}
                    className={`filter-choice-pill ${isActive ? "active" : ""}`}
                  >
                    {o.name}
                  </a>
                );
              })}
            </div>
          </div>

          {/* 4. Flower Types & Cultivars */}
          <div className="filter-group">
            <div className="filter-section-header">
              <span className="filter-section-title">Loại hoa & Giống hoa</span>
              {flowerType && (
                <a
                  href={buildFilterUrl(currentParams, { flower_type: null })}
                  style={{ fontSize: "0.78rem", color: "var(--accent-peach)", textDecoration: "none" }}
                >
                  Bỏ chọn
                </a>
              )}
            </div>

            {/* Popular Curated Pills */}
            <div className="filter-choice-pills" style={{ marginBottom: "0.85rem" }}>
              <a
                href={buildFilterUrl(currentParams, { flower_type: null })}
                className={`filter-choice-pill ${!flowerType ? "active" : ""}`}
              >
                Tất cả
              </a>
              {popularFlowers.map((pf) => {
                const isActive = flowerType === pf.slug;
                return (
                  <a
                    key={pf.slug}
                    href={buildFilterUrl(currentParams, { flower_type: isActive ? null : pf.slug })}
                    className={`filter-choice-pill ${isActive ? "active" : ""}`}
                  >
                    {pf.name}
                  </a>
                );
              })}
            </div>

            {/* Full 110+ Reference Taxonomy Dropdown */}
            {flowerTypes.length > 0 && (
              <form method="GET" action="/san-pham" style={{ marginTop: "0.6rem" }}>
                {category && <input type="hidden" name="category" value={category} />}
                {occasion && <input type="hidden" name="occasion" value={occasion} />}
                {color && <input type="hidden" name="color" value={color} />}
                {budget && <input type="hidden" name="budget" value={budget} />}
                {params.min_price && <input type="hidden" name="min_price" value={params.min_price} />}
                {params.max_price && <input type="hidden" name="max_price" value={params.max_price} />}
                {q && <input type="hidden" name="q" value={q} />}
                {sort !== "newest" && <input type="hidden" name="sort" value={sort} />}

                <label
                  htmlFor="quick-lookup-select"
                  style={{ fontSize: "0.78rem", fontWeight: 700, color: "var(--text-muted)", marginBottom: "0.35rem", display: "block" }}
                >
                  Tra cứu nhanh 110+ loài hoa & phụ liệu:
                </label>
                <div style={{ display: "flex", gap: "0.4rem" }}>
                  <select
                    id="quick-lookup-select"
                    name="flower_type"
                    defaultValue={flowerType}
                    style={{
                      flex: 1,
                      padding: "0.48rem 0.65rem",
                      fontSize: "0.82rem",
                      borderRadius: "var(--radius-sm)",
                      border: "1px solid rgba(24, 61, 45, 0.18)",
                      background: "#FAF8F4",
                    }}
                  >
                    <option value="">-- Danh mục 110+ loài hoa --</option>
                    {flowerTypes.map((ft) => (
                      <option key={ft.id} value={ft.slug}>
                        {ft.name}
                      </option>
                    ))}
                  </select>
                  <button type="submit" className="button button-sm button-secondary" style={{ padding: "0.45rem 0.75rem", whiteSpace: "nowrap" }}>
                    Lọc
                  </button>
                </div>
              </form>
            )}
          </div>

          {/* 5. Budget & Price Range */}
          <div className="filter-group">
            <div className="filter-section-header">
              <span className="filter-section-title">Mức giá ngân sách</span>
              {(budget || params.min_price || params.max_price) && (
                <a
                  href={buildFilterUrl(currentParams, { budget: null, min_price: null, max_price: null })}
                  style={{ fontSize: "0.78rem", color: "var(--accent-peach)", textDecoration: "none" }}
                >
                  Bỏ chọn
                </a>
              )}
            </div>

            {/* Preset Budget Pills */}
            <div className="filter-choice-pills">
              <a
                href={buildFilterUrl(currentParams, { budget: null, min_price: null, max_price: null })}
                className={`filter-choice-pill ${!budget && !params.min_price && !params.max_price ? "active" : ""}`}
              >
                Tất cả mức giá
              </a>
              <a
                href={buildFilterUrl(currentParams, { budget: budget === "under500" ? null : "under500", min_price: null, max_price: null })}
                className={`filter-choice-pill ${budget === "under500" ? "active" : ""}`}
              >
                &lt; 500k
              </a>
              <a
                href={buildFilterUrl(currentParams, { budget: budget === "500to1000" ? null : "500to1000", min_price: null, max_price: null })}
                className={`filter-choice-pill ${budget === "500to1000" ? "active" : ""}`}
              >
                500k - 1 triệu
              </a>
              <a
                href={buildFilterUrl(currentParams, { budget: budget === "1000to2000" ? null : "1000to2000", min_price: null, max_price: null })}
                className={`filter-choice-pill ${budget === "1000to2000" ? "active" : ""}`}
              >
                1 - 2 triệu
              </a>
              <a
                href={buildFilterUrl(currentParams, { budget: budget === "over2000" ? null : "over2000", min_price: null, max_price: null })}
                className={`filter-choice-pill ${budget === "over2000" ? "active" : ""}`}
              >
                &gt; 2 triệu
              </a>
            </div>

            {/* Custom Min / Max Inputs */}
            <form method="GET" action="/san-pham" style={{ marginTop: "0.9rem" }}>
              {category && <input type="hidden" name="category" value={category} />}
              {occasion && <input type="hidden" name="occasion" value={occasion} />}
              {flowerType && <input type="hidden" name="flower_type" value={flowerType} />}
              {color && <input type="hidden" name="color" value={color} />}
              {q && <input type="hidden" name="q" value={q} />}
              {sort !== "newest" && <input type="hidden" name="sort" value={sort} />}

              <div style={{ fontSize: "0.78rem", fontWeight: 700, color: "var(--text-muted)", marginBottom: "0.3rem" }}>
                Hoặc nhập khoảng giá (VNĐ):
              </div>
              <div className="custom-price-inputs">
                <input
                  type="number"
                  name="min_price"
                  placeholder="Từ (đ)"
                  defaultValue={params.min_price || ""}
                  step="50000"
                  min="0"
                />
                <span style={{ color: "var(--text-muted)", fontWeight: 700 }}>-</span>
                <input
                  type="number"
                  name="max_price"
                  placeholder="Đến (đ)"
                  defaultValue={params.max_price || ""}
                  step="50000"
                  min="0"
                />
              </div>
              <button
                type="submit"
                className="button button-secondary button-sm"
                style={{ width: "100%", marginTop: "0.55rem" }}
              >
                Áp dụng khoảng giá
              </button>
            </form>
          </div>

          {/* Reset All Filter Action */}
          <div style={{ marginTop: "1.4rem", paddingTop: "1.2rem", borderTop: "1px solid rgba(24, 61, 45, 0.08)" }}>
            <a
              href="/san-pham"
              className="button button-secondary"
              style={{ width: "100%", textAlign: "center", fontSize: "0.88rem" }}
            >
              Đặt lại tất cả bộ lọc
            </a>
          </div>
        </aside>

        {/* Right Section: Top Controls & Product Grid */}
        <section>
          {/* Top Controls Bar */}
          <div className="catalog-top-controls">
            <div className="results-count-badge">
              Hiển thị <strong>{items ? items.length : 0}</strong> mẫu hoa nghệ thuật
            </div>

            {/* Sort Form */}
            <form method="GET" action="/san-pham" className="sort-select-wrapper">
              {q && <input type="hidden" name="q" value={q} />}
              {category && <input type="hidden" name="category" value={category} />}
              {occasion && <input type="hidden" name="occasion" value={occasion} />}
              {flowerType && <input type="hidden" name="flower_type" value={flowerType} />}
              {color && <input type="hidden" name="color" value={color} />}
              {budget && <input type="hidden" name="budget" value={budget} />}
              {params.min_price && <input type="hidden" name="min_price" value={params.min_price} />}
              {params.max_price && <input type="hidden" name="max_price" value={params.max_price} />}

              <label
                htmlFor="sort-select"
                style={{ fontSize: "0.85rem", fontWeight: 650, color: "var(--text-muted)", marginBottom: 0, whiteSpace: "nowrap" }}
              >
                Sắp xếp:
              </label>
              <select
                id="sort-select"
                name="sort"
                defaultValue={sort}
                style={{ padding: "0.45rem 1.6rem 0.45rem 0.85rem" }}
              >
                <option value="newest">✨ Mới nhất</option>
                <option value="price_asc">💵 Giá: Thấp đến cao</option>
                <option value="price_desc">💎 Giá: Cao đến thấp</option>
                <option value="name">🔤 Tên A-Z</option>
              </select>
              <button type="submit" className="button button-sm button-secondary">
                Lọc
              </button>
            </form>
          </div>

          {/* Product Items List or Fallback / Empty States */}
          {items === null ? (
            <div className="panel" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
              <span style={{ fontSize: "3rem", display: "block", marginBottom: "1rem" }}>⚠️</span>
              <p role="alert" style={{ fontSize: "1.05rem", fontWeight: 600 }}>
                Không thể kết nối đến máy chủ danh mục lúc này. Vui lòng thử lại sau.
              </p>
            </div>
          ) : items.length === 0 ? (
            <div className="panel" style={{ textAlign: "center", padding: "3.5rem 1.5rem" }}>
              <span style={{ fontSize: "3.5rem", display: "block", marginBottom: "1rem" }}>🌸</span>
              <h3 style={{ marginBottom: "0.6rem" }}>Không tìm thấy mẫu hoa phù hợp</h3>
              <p style={{ color: "var(--text-muted)", maxWidth: "460px", margin: "0 auto 1.6rem" }}>
                Không có sản phẩm nào thỏa mãn toàn bộ tiêu chí lọc hiện tại. Bạn vui lòng thử xóa bớt điều kiện hoặc xem toàn bộ danh mục hoa.
              </p>
              <a href="/san-pham" className="button">
                Khám phá tất cả mẫu hoa
              </a>
            </div>
          ) : (
            <div className="product-grid">
              {items.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

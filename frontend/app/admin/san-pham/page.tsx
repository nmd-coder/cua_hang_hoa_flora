import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { adminCreateProduct, adminToggleProductStatus } from "../../actions";
import { getCategories, price, type Product, type Category } from "../../../lib/catalog";

const base = process.env.GATEWAY_URL || "http://localhost:8080";

export default async function AdminProductsPage() {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  let products: Product[] = [];
  let categories: Category[] = [];

  try {
    const [pRes, cList] = await Promise.all([
      fetch(base + "/api/products?status=ALL&size=100", { cache: "no-store" }),
      getCategories(),
    ]);
    if (pRes.ok) products = await pRes.json();
    categories = cList;
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.5rem" }}>
        <div>
          <p className="eyebrow">QUẢN LÝ DANH MỤC</p>
          <h1>Quản lý sản phẩm hoa tươi</h1>
        </div>
        <a href="/admin" className="button button-sm button-secondary">← Về Bảng điều khiển</a>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1.4fr 0.6fr", gap: "2rem", alignItems: "flex-start" }}>
        {/* Products List Table */}
        <div className="panel">
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Danh sách sản phẩm ({products.length})</h2>

          <table className="data-table">
            <thead>
              <tr>
                <th>Ảnh</th>
                <th>Tên sản phẩm / SKU</th>
                <th>Danh mục</th>
                <th>Giá bán</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td>
                    <div style={{ width: "45px", height: "45px", borderRadius: "4px", background: "#EFE9E4", overflow: "hidden" }}>
                      {p.image ? (
                        <img src={p.image} alt={p.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                      ) : (
                        <div style={{ width: "100%", height: "100%", display: "grid", placeItems: "center" }}>✿</div>
                      )}
                    </div>
                  </td>
                  <td>
                    <strong>{p.name}</strong><br />
                    <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>SKU: {p.sku}</span>
                  </td>
                  <td>{p.category_name || "—"}</td>
                  <td><strong>{price(p.price)}</strong></td>
                  <td>
                    <span className={`status-pill ${p.status === "ACTIVE" ? "status-COMPLETED" : "status-CANCELLED"}`}>
                      {p.status}
                    </span>
                  </td>
                  <td>
                    <form action={adminToggleProductStatus}>
                      <input type="hidden" name="id" value={p.id} />
                      <input type="hidden" name="currentStatus" value={p.status} />
                      <button type="submit" className="button button-sm button-secondary">
                        {p.status === "ACTIVE" ? "Ẩn bán" : "Hiện bán"}
                      </button>
                    </form>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Add Product Form */}
        <div className="panel">
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Thêm sản phẩm mới</h2>
          <form action={adminCreateProduct}>
            <label>
              Tên sản phẩm hoa
              <input name="name" required placeholder="Ví dụ: Bó Hoa Nắng Sớm" />
            </label>

            <label>
              Mã SKU (tùy chọn)
              <input name="sku" placeholder="FL-XXX (tự sinh nếu để trống)" />
            </label>

            <label>
              Danh mục
              <select name="categoryId">
                <option value="">-- Chọn danh mục --</option>
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </label>

            <label>
              Giá bán (VND)
              <input name="price" type="number" required min={0} step={1000} placeholder="350000" />
            </label>

            <label>
              Mô tả sản phẩm
              <textarea name="description" placeholder="Mô tả ý nghĩa, loại hoa, dịp tặng..." />
            </label>

            <label>
              Đường dẫn hình ảnh (URL)
              <input name="imageUrl" placeholder="https://images.unsplash.com/..." />
            </label>

            <button type="submit" style={{ width: "100%", marginTop: "1rem" }}>
              Tạo sản phẩm
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}

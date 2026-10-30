import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { adminAdjustStock } from "../../actions";
import { products, type Product } from "../../../lib/catalog";

const base = process.env.GATEWAY_URL || "http://localhost:8080";

type StockItem = {
  item_id: string;
  on_hand: number;
  reserved: number;
  available: number;
  updated_at: string;
};

type Movement = {
  id: string;
  item_id: string;
  change_qty: number;
  reason: string;
  created_at: string;
};

export default async function AdminInventoryPage() {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  let stockList: StockItem[] = [];
  let movements: Movement[] = [];
  const productMap: Record<string, Product> = {};

  try {
    const [stockRes, movRes, pList] = await Promise.all([
      fetch(base + "/api/inventory/stock", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
      fetch(base + "/api/inventory/movements?limit=30", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
      products(""),
    ]);

    if (stockRes.ok) stockList = await stockRes.json();
    if (movRes.ok) movements = await movRes.json();
    pList.forEach(p => { productMap[p.id] = p; });
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.5rem" }}>
        <div>
          <p className="eyebrow">QUẢN TRỊ KHO</p>
          <h1>Quản lý tồn kho hoa tươi</h1>
        </div>
        <a href="/admin" className="button button-sm button-secondary">← Về Bảng điều khiển</a>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1.3fr 0.7fr", gap: "2rem", alignItems: "flex-start" }}>
        {/* Stock Levels Table */}
        <div className="panel">
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Tình trạng tồn kho hiện tại</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Hiện có</th>
                <th>Đang giữ</th>
                <th>Khả dụng</th>
                <th>Tình trạng</th>
              </tr>
            </thead>
            <tbody>
              {stockList.map(s => {
                const prod = productMap[s.item_id];
                const isLow = s.available <= 10;
                return (
                  <tr key={s.item_id}>
                    <td>
                      <strong>{prod?.name || s.item_id.substring(0, 8)}</strong><br />
                      <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                        {prod ? `SKU: ${prod.sku}` : s.item_id}
                      </span>
                    </td>
                    <td>{s.on_hand}</td>
                    <td style={{ color: s.reserved > 0 ? "#C28E34" : "inherit" }}>{s.reserved}</td>
                    <td>
                      <strong style={{ color: isLow ? "#C5221F" : "#137333", fontSize: "1.05rem" }}>
                        {s.available}
                      </strong>
                    </td>
                    <td>
                      <span className={`stock-badge ${s.available > 0 ? "stock-in" : "stock-out"}`}>
                        {s.available > 10 ? "Đủ hàng" : s.available > 0 ? "Sắp hết" : "Hết hàng"}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Adjust Stock Form */}
        <div>
          <div className="panel">
            <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Điều chỉnh tồn kho</h2>
            <form action={adminAdjustStock}>
              <label>
                Chọn sản phẩm
                <select name="itemId" required>
                  <option value="">-- Chọn sản phẩm --</option>
                  {stockList.map(s => {
                    const prod = productMap[s.item_id];
                    return (
                      <option key={s.item_id} value={s.item_id}>
                        {prod ? prod.name : s.item_id.substring(0, 8)} (Khả dụng: {s.available})
                      </option>
                    );
                  })}
                </select>
              </label>

              <label>
                Số lượng thay đổi (+ để nhập thêm, - để xuất bớt)
                <input name="changeQty" type="number" required placeholder="Ví dụ: 10 hoặc -5" />
              </label>

              <label>
                Lý do điều chỉnh (bắt buộc)
                <input name="reason" required placeholder="Ví dụ: Nhập hàng Đà Lạt ngày 30/10, Hủy hoa héo..." />
              </label>

              <p style={{ fontSize: "0.8rem", color: "var(--text-muted)", marginTop: "0.4rem" }}>
                * Quy tắc: Tồn khả dụng không được âm và không được giảm dưới lượng đang giữ hàng cho đơn đang đặt.
              </p>

              <button type="submit" style={{ width: "100%", marginTop: "1rem" }}>
                Xác nhận điều chỉnh
              </button>
            </form>
          </div>

          {/* Recent movements */}
          <div className="panel">
            <h3 style={{ fontSize: "1.1rem", marginBottom: "0.8rem" }}>Nhật ký biến động gần đây</h3>
            <div style={{ display: "flex", flexDirection: "column", gap: "0.6rem", maxHeight: "300px", overflowY: "auto" }}>
              {movements.map(m => {
                const prod = productMap[m.item_id];
                const isPositive = m.change_qty > 0;
                return (
                  <div key={m.id} style={{ fontSize: "0.85rem", paddingBottom: "0.4rem", borderBottom: "1px solid var(--border)" }}>
                    <div style={{ display: "flex", justifyContent: "space-between" }}>
                      <strong>{prod ? prod.name : m.item_id.substring(0, 8)}</strong>
                      <span style={{ fontWeight: 700, color: isPositive ? "#137333" : "#C5221F" }}>
                        {isPositive ? `+${m.change_qty}` : m.change_qty}
                      </span>
                    </div>
                    <p style={{ color: "var(--text-muted)", margin: "0.2rem 0 0" }}>
                      {m.reason} · {new Date(m.created_at).toLocaleString("vi-VN")}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}


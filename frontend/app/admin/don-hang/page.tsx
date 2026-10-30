import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { adminUpdateOrderStatus } from "../../actions";
import { price } from "../../../lib/catalog";

const base = process.env.GATEWAY_URL || "http://localhost:8080";

type Order = {
  id: string;
  order_code: string;
  user_id: string;
  status: string;
  recipient_name: string;
  recipient_phone: string;
  delivery_date: string;
  delivery_time_slot: string;
  subtotal: number;
  discount_amount: number;
  shipping_fee: number;
  total: number;
  payment_method: string;
  payment_status: string;
  voucher_code?: string;
  created_at: string;
  items: {
    id: string;
    name_snapshot: string;
    unit_price: number;
    quantity: number;
    line_total: number;
  }[];
};

type Refund = {
  id: string;
  order_id: string;
  amount: number;
  status: string;
  reason: string;
  created_at: string;
};

export default async function AdminOrdersPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string; q?: string; error?: string; notice?: string }>;
}) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  const query = await searchParams;
  const statusFilter = query.status || "";
  const q = query.q || "";

  let orders: Order[] = [];
  let refunds: Refund[] = [];

  try {
    const url = new URL("/api/admin/orders", base);
    if (statusFilter) url.searchParams.set("status", statusFilter);
    if (q) url.searchParams.set("q", q);
    url.searchParams.set("size", "100");

    const [oRes, rRes] = await Promise.all([
      fetch(url.toString(), { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
      fetch(base + "/api/payments/refunds", { cache: "no-store" }),
    ]);

    if (oRes.ok) orders = await oRes.json();
    if (rRes.ok) refunds = await rRes.json();
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.5rem" }}>
        <div>
          <p className="eyebrow">QUẢN TRỊ ĐƠN HÀNG</p>
          <h1>Quản lý toàn bộ đơn hàng</h1>
        </div>
        <a href="/admin" className="button button-sm button-secondary">← Về Bảng điều khiển</a>
      </div>

      {query.error && <p role="alert">{query.error}</p>}
      {query.notice && <p role="status">{query.notice}</p>}

      {/* Status filter tabs */}
      <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", margin: "1rem 0" }}>
        {[
          { label: "Tất cả", val: "" },
          { label: "PENDING", val: "PENDING" },
          { label: "PROCESSING", val: "PROCESSING" },
          { label: "SHIPPED", val: "SHIPPED" },
          { label: "COMPLETED", val: "COMPLETED" },
          { label: "RECEIVED", val: "RECEIVED" },
          { label: "CANCELLED", val: "CANCELLED" },
        ].map(tab => (
          <a
            key={tab.val}
            href={`/admin/don-hang${tab.val ? "?status=" + tab.val : ""}`}
            className={`button button-sm ${statusFilter === tab.val ? "" : "button-secondary"}`}
          >
            {tab.label}
          </a>
        ))}
      </div>

      {/* Orders List */}
      <div className="panel" style={{ marginBottom: "2rem" }}>
        <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Danh sách đơn hàng ({orders.length})</h2>

        <table className="data-table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Người nhận</th>
              <th>Ngày giao</th>
              <th>Tổng tiền</th>
              <th>Thanh toán</th>
              <th>Trạng thái</th>
              <th>Chuyển trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(o => (
              <tr key={o.id}>
                <td>
                  <a href={`/don-hang/${o.id}`}><strong>{o.order_code}</strong></a><br />
                  <span style={{ fontSize: "0.78rem", color: "var(--text-muted)" }}>
                    {new Date(o.created_at).toLocaleDateString("vi-VN")}
                  </span>
                </td>
                <td>
                  {o.recipient_name}<br />
                  <span style={{ fontSize: "0.82rem", color: "var(--text-muted)" }}>{o.recipient_phone}</span>
                </td>
                <td>
                  {o.delivery_date}<br />
                  <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>{o.delivery_time_slot}</span>
                </td>
                <td>
                  <strong>{price(o.total)}</strong>
                  {o.discount_amount > 0 && (
                    <div style={{ fontSize: "0.78rem", color: "#137333" }}>
                      Voucher: -{price(o.discount_amount)}
                    </div>
                  )}
                </td>
                <td>
                  <span style={{ fontSize: "0.85rem" }}>{o.payment_method}</span><br />
                  <span style={{ fontSize: "0.8rem", color: o.payment_status === "PAID" ? "#137333" : "var(--text-muted)" }}>
                    {o.payment_status}
                  </span>
                </td>
                <td>
                  <span className={`status-pill status-${o.status}`}>{o.status}</span>
                </td>
                <td>
                  {o.status === "PENDING" && (
                    <form action={adminUpdateOrderStatus} style={{ display: "inline" }}>
                      <input type="hidden" name="orderId" value={o.id} />
                      <input type="hidden" name="status" value="PROCESSING" />
                      <button type="submit" className="button button-sm">Duyệt đơn</button>
                    </form>
                  )}
                  {o.status === "PROCESSING" && (
                    <form action={adminUpdateOrderStatus} style={{ display: "inline" }}>
                      <input type="hidden" name="orderId" value={o.id} />
                      <input type="hidden" name="status" value="SHIPPED" />
                      <button type="submit" className="button button-sm">Giao hàng</button>
                    </form>
                  )}
                  {o.status === "SHIPPED" && (
                    <form action={adminUpdateOrderStatus} style={{ display: "inline" }}>
                      <input type="hidden" name="orderId" value={o.id} />
                      <input type="hidden" name="status" value="COMPLETED" />
                      <button type="submit" className="button button-sm" style={{ background: "#2A7E4B" }}>Hoàn tất</button>
                    </form>
                  )}
                  {["PENDING", "PROCESSING"].includes(o.status) && (
                    <form action={adminUpdateOrderStatus} style={{ display: "inline", marginLeft: "0.4rem" }}>
                      <input type="hidden" name="orderId" value={o.id} />
                      <input type="hidden" name="status" value="CANCELLED" />
                      <input type="hidden" name="reason" value="Quản trị viên hủy đơn" />
                      <button type="submit" className="button button-sm button-danger">Hủy</button>
                    </form>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Refunds Section */}
      <div className="panel">
        <h2 style={{ fontSize: "1.2rem", marginBottom: "0.8rem" }}>Lịch sử hoàn tiền ({refunds.length})</h2>
        {refunds.length === 0 ? (
          <p style={{ color: "var(--text-muted)", fontSize: "0.9rem" }}>Chưa có yêu cầu hoàn tiền nào.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã đơn hàng</th>
                <th>Số tiền hoàn</th>
                <th>Lý do</th>
                <th>Trạng thái</th>
                <th>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              {refunds.map(r => (
                <tr key={r.id}>
                  <td><code>{r.order_id}</code></td>
                  <td><strong style={{ color: "#C5221F" }}>{price(r.amount)}</strong></td>
                  <td>{r.reason || "Hủy đơn hàng"}</td>
                  <td><span className="status-pill status-COMPLETED">{r.status}</span></td>
                  <td>{new Date(r.created_at).toLocaleString("vi-VN")}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}


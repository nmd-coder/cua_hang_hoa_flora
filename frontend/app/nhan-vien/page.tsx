import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { staffUpdateOrderStatus } from "../actions";
import { price } from "../../lib/catalog";
import { formatOrderStatus, formatPaymentStatus } from "../../lib/order";

import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();

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
    product_id: string;
    sku_snapshot: string;
    name_snapshot: string;
    unit_price: number;
    quantity: number;
    line_total: number;
  }[];
};

export default async function StaffPortalPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string; q?: string; error?: string; notice?: string }>;
}) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  // Verify staff or admin role
  let isStaff = false;
  try {
    const meRes = await fetch(getBase() + "/api/auth/me", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
    if (meRes.ok) {
      const p = await meRes.json() as { roles?: string[] };
      isStaff = p.roles?.includes("STAFF") || p.roles?.includes("ADMIN") || false;
    }
  } catch {}

  if (!isStaff) {
    redirect("/tai-khoan?error=B%E1%BA%A1n%20kh%C3%B4ng%20c%C3%B3%20quy%E1%BB%81n%20truy%20c%E1%BA%ADp%20khu%20v%E1%BB%B1c%20nh%C3%A2n%20vi%C3%AAn");
  }

  const query = await searchParams;
  const statusFilter = query.status || "";
  const searchKeyword = query.q || "";

  let orders: Order[] = [];
  try {
    const url = new URL("/api/admin/orders", getBase());
    if (statusFilter) url.searchParams.set("status", statusFilter);
    if (searchKeyword) url.searchParams.set("q", searchKeyword);
    url.searchParams.set("size", "50");

    const res = await fetch(url.toString(), {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
    if (res.ok) orders = await res.json();
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.5rem" }}>
        <div>
          <p className="eyebrow">KHU VỰC VẬN HÀNH</p>
          <h1>Quản lý xử lý đơn hàng</h1>
        </div>
        <a href="/admin" className="button button-sm button-secondary">Vào trang Quản trị viên →</a>
      </div>

      {query.error && <p role="alert">{query.error}</p>}
      {query.notice && <p role="status">{query.notice}</p>}

      {/* Filter Tabs */}
      <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", margin: "1rem 0" }}>
        {[
          { label: "Tất cả", val: "" },
          { label: "Chờ tiếp nhận", val: "PENDING" },
          { label: "Đang chuẩn bị hoa", val: "PROCESSING" },
          { label: "Đang giao", val: "SHIPPED" },
          { label: "Hoàn tất", val: "COMPLETED" },
          { label: "Đã hủy", val: "CANCELLED" },
        ].map(tab => (
          <a
            key={tab.val}
            href={`/nhan-vien${tab.val ? "?status=" + tab.val : ""}`}
            className={`button button-sm ${statusFilter === tab.val ? "" : "button-secondary"}`}
          >
            {tab.label}
          </a>
        ))}
      </div>

      {/* Search box */}
      <form method="GET" action="/nhan-vien" style={{ display: "flex", gap: "0.5rem", marginBottom: "1.5rem", maxWidth: "500px" }}>
        {statusFilter && <input type="hidden" name="status" value={statusFilter} />}
        <input name="q" defaultValue={searchKeyword} placeholder="Tìm theo mã đơn, tên hoặc SĐT người nhận..." />
        <button type="submit" className="button button-secondary">Tìm</button>
      </form>

      {/* Orders Table */}
      {orders.length === 0 ? (
        <div className="panel" style={{ textAlign: "center", padding: "2.5rem" }}>
          <p style={{ color: "var(--text-muted)", margin: 0 }}>Không có đơn hàng nào cần xử lý trong mục này.</p>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
          {orders.map(o => (
            <div key={o.id} className="panel" style={{ margin: 0 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", flexWrap: "wrap", gap: "1rem", marginBottom: "0.8rem" }}>
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.8rem" }}>
                    <strong style={{ fontSize: "1.2rem" }}>{o.order_code}</strong>
                    <span className={`status-pill status-${o.status}`}>{formatOrderStatus(o.status)}</span>
                    <span style={{ fontSize: "0.82rem", color: "var(--text-muted)" }}>
                      Đặt lúc: {new Date(o.created_at).toLocaleString("vi-VN")}
                    </span>
                  </div>

                  <p style={{ fontSize: "0.9rem", margin: "0.3rem 0" }}>
                    <strong>Người nhận:</strong> {o.recipient_name} ({o.recipient_phone}) · Lịch giao: <strong>{o.delivery_date}</strong> {o.delivery_time_slot && `(${o.delivery_time_slot})`}
                  </p>
                </div>

                <div style={{ textAlign: "right" }}>
                  <div style={{ fontSize: "1.2rem", fontWeight: 700, color: "var(--primary)" }}>
                    {price(o.total)}
                  </div>
                  <span style={{ fontSize: "0.82rem", color: "var(--text-muted)" }}>
                    COD: {formatPaymentStatus(o.payment_status)}
                  </span>
                </div>
              </div>

              {/* Items summary */}
              <div style={{ background: "#FAF8F4", padding: "0.6rem 0.8rem", borderRadius: "var(--radius-sm)", marginBottom: "0.8rem", fontSize: "0.88rem" }}>
                {o.items?.map(it => (
                  <span key={it.id} style={{ marginRight: "1rem", display: "inline-block" }}>
                    • {it.name_snapshot} × <strong>{it.quantity}</strong> ({price(it.line_total)})
                  </span>
                ))}
              </div>

              {/* Action buttons depending on state */}
              <div style={{ display: "flex", gap: "0.6rem", flexWrap: "wrap", alignItems: "center" }}>
                {o.status === "PENDING" && (
                  <form action={staffUpdateOrderStatus}>
                    <input type="hidden" name="orderId" value={o.id} />
                    <input type="hidden" name="status" value="PROCESSING" />
                    <button type="submit" className="button button-sm">
                      ✓ Tiếp nhận đơn (Bắt đầu cắm hoa)
                    </button>
                  </form>
                )}

                {o.status === "PROCESSING" && (
                  <form action={staffUpdateOrderStatus}>
                    <input type="hidden" name="orderId" value={o.id} />
                    <input type="hidden" name="status" value="SHIPPED" />
                    <button type="submit" className="button button-sm">
                      🚚 Bàn giao giao hàng (Đang giao)
                    </button>
                  </form>
                )}

                {o.status === "SHIPPED" && (
                  <form action={staffUpdateOrderStatus}>
                    <input type="hidden" name="orderId" value={o.id} />
                    <input type="hidden" name="status" value="COMPLETED" />
                    <button type="submit" className="button button-sm" style={{ background: "#2A7E4B" }}>
                      ★ Hoàn tất giao hàng
                    </button>
                  </form>
                )}

                {["PENDING", "PROCESSING"].includes(o.status) && (
                  <form action={staffUpdateOrderStatus}>
                    <input type="hidden" name="orderId" value={o.id} />
                    <input type="hidden" name="status" value="CANCELLED" />
                    <input type="hidden" name="reason" value="Nhân viên hủy theo yêu cầu" />
                    <button type="submit" className="button button-sm button-danger">
                      ✕ Hủy đơn
                    </button>
                  </form>
                )}

                <a href={`/don-hang/${o.id}`} className="button button-sm button-secondary">
                  Xem chi tiết
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}


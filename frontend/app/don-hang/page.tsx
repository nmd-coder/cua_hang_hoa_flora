import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { gateway } from "../../lib/checkout";
import { price } from "../../lib/catalog";

type OrderItem = {
  id: string;
  order_code: string;
  status: string;
  total: number;
  payment_method: string;
  payment_status: string;
  created_at: string;
  recipient_name?: string;
  delivery_date?: string;
};

export default async function OrdersPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string; error?: string; notice?: string }>;
}) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=Vui%20l%C3%B2ng%20%C4%91%C4%83ng%20nh%E1%BA%ADp%20%C4%91%E1%BB%83%20xem%20%C4%91%C6%A1n%20h%C3%A0ng");

  const query = await searchParams;
  const statusFilter = query.status || "";

  let orders: OrderItem[] | null = null;
  try {
    const r = await fetch(gateway + "/api/orders", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
    if (r.ok) orders = (await r.json()) as OrderItem[];
  } catch {}

  const filteredOrders = orders
    ? orders.filter(o => !statusFilter || o.status === statusFilter)
    : [];

  return (
    <section>
      <p className="eyebrow">LỊCH SỬ MUA HÀNG</p>
      <h1>Đơn hàng của bạn</h1>

      {query.error && <p role="alert">{query.error}</p>}
      {query.notice && <p role="status">{query.notice}</p>}

      {/* Filter Tabs */}
      <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap", margin: "1.5rem 0" }}>
        {[
          { label: "Tất cả", val: "" },
          { label: "Chờ xử lý (PENDING)", val: "PENDING" },
          { label: "Đang cắm hoa (PROCESSING)", val: "PROCESSING" },
          { label: "Đang giao (SHIPPED)", val: "SHIPPED" },
          { label: "Đã giao (COMPLETED)", val: "COMPLETED" },
          { label: "Đã nhận (RECEIVED)", val: "RECEIVED" },
          { label: "Đã hủy (CANCELLED)", val: "CANCELLED" },
        ].map(tab => (
          <a
            key={tab.val}
            href={`/don-hang${tab.val ? "?status=" + tab.val : ""}`}
            className={`button button-sm ${statusFilter === tab.val ? "" : "button-secondary"}`}
          >
            {tab.label}
          </a>
        ))}
      </div>

      {orders === null ? (
        <p role="alert">Không thể tải đơn hàng lúc này. Vui lòng kiểm tra phiên đăng nhập và thử lại.</p>
      ) : filteredOrders.length === 0 ? (
        <div className="panel" style={{ textAlign: "center", padding: "3rem" }}>
          <span style={{ fontSize: "3rem", display: "block", marginBottom: "0.8rem" }}>📦</span>
          <h3>{statusFilter ? "Không có đơn hàng nào ở trạng thái này." : "Bạn chưa có đơn hàng nào."}</h3>
          <p style={{ color: "var(--text-muted)", margin: "0.5rem 0 1.5rem" }}>
            Mỗi bó hoa trao gửi là một lời nhắn trọn vẹn tình cảm.
          </p>
          <a href="/san-pham" className="button">Khám phá hoa ngay</a>
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
          {filteredOrders.map(o => (
            <article className="panel" key={o.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "1rem" }}>
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: "0.8rem", marginBottom: "0.4rem" }}>
                  <a href={"/don-hang/" + o.id} style={{ fontSize: "1.2rem", fontWeight: 700 }}>
                    {o.order_code}
                  </a>
                  <span className={`status-pill status-${o.status}`}>
                    {o.status}
                  </span>
                </div>

                <p style={{ fontSize: "0.88rem", color: "var(--text-muted)", margin: 0 }}>
                  Ngày đặt: {new Date(o.created_at).toLocaleDateString("vi-VN")} · Người nhận: {o.recipient_name || "—"}
                </p>
                {o.delivery_date && (
                  <p style={{ fontSize: "0.85rem", color: "var(--text-muted)", margin: 0 }}>
                    Lịch giao: {o.delivery_date}
                  </p>
                )}
              </div>

              <div style={{ textAlign: "right" }}>
                <div style={{ fontSize: "1.2rem", fontWeight: 700, color: "var(--primary)", marginBottom: "0.4rem" }}>
                  {price(o.total)}
                </div>
                <a href={"/don-hang/" + o.id} className="button button-sm button-secondary">
                  Chi tiết đơn hàng →
                </a>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

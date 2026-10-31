import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { price } from "../../lib/catalog";
import { formatOrderStatus } from "../../lib/order";

import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();

type DashboardStats = {
  newOrders: number;
  processingOrders: number;
  shippingOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  totalRevenue: number;
  recentOrders: {
    id: string;
    order_code: string;
    recipient_name: string;
    total: number;
    status: string;
    created_at: string;
  }[];
};

type StockItem = {
  item_id: string;
  on_hand: number;
  reserved: number;
  available: number;
};

export default async function AdminDashboardPage() {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  // Check admin role
  let isAdmin = false;
  try {
    const meRes = await fetch(getBase() + "/api/auth/me", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
    if (meRes.ok) {
      const p = await meRes.json() as { roles?: string[] };
      isAdmin = p.roles?.includes("ADMIN") || false;
    }
  } catch {}

  if (!isAdmin) {
    redirect("/tai-khoan?error=Y%C3%AAu%20c%E1%BA%A7u%20quy%E1%BB%81n%20Qu%E1%BA%A3n%20tr%E1%BB%8B%20vi%C3%AAn");
  }

  let stats: DashboardStats = {
    newOrders: 0,
    processingOrders: 0,
    shippingOrders: 0,
    completedOrders: 0,
    cancelledOrders: 0,
    totalRevenue: 0,
    recentOrders: [],
  };

  let lowStockCount = 0;

  try {
    const [dashRes, stockRes] = await Promise.all([
      fetch(getBase() + "/api/admin/dashboard", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
      fetch(getBase() + "/api/inventory/stock", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
    ]);

    if (dashRes.ok) stats = await dashRes.json();
    if (stockRes.ok) {
      const stockList = (await stockRes.json()) as StockItem[];
      lowStockCount = stockList.filter(s => s.available <= 10).length;
    }
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "2rem", flexWrap: "wrap", gap: "1rem" }}>
        <div>
          <p className="eyebrow">QUẢN TRỊ VIÊN</p>
          <h1>Bảng điều khiển kinh doanh</h1>
        </div>

        {/* Admin Navigation Pills */}
        <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
          <a href="/admin/san-pham" className="button button-sm button-secondary">Quản lý Sản phẩm</a>
          <a href="/admin/ton-kho" className="button button-sm button-secondary">Quản lý Tồn kho</a>
          <a href="/admin/vouchers" className="button button-sm button-secondary">Quản lý Voucher</a>
          <a href="/admin/don-hang" className="button button-sm button-secondary">Quản lý Đơn hàng</a>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "1.2rem", marginBottom: "2.5rem" }}>
        <div className="panel" style={{ margin: 0, borderTop: "4px solid var(--primary)" }}>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>Tổng doanh thu thực tế</span>
          <div style={{ fontSize: "1.8rem", fontWeight: 700, color: "var(--primary)", margin: "0.4rem 0" }}>
            {price(stats.totalRevenue)}
          </div>
          <span style={{ fontSize: "0.78rem", color: "#137333" }}>Từ đơn đã giao & nhận</span>
        </div>

        <div className="panel" style={{ margin: 0, borderTop: "4px solid #C28E34" }}>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>Đơn hàng mới chờ duyệt</span>
          <div style={{ fontSize: "1.8rem", fontWeight: 700, color: "#C28E34", margin: "0.4rem 0" }}>
            {stats.newOrders}
          </div>
          <a href="/admin/don-hang?status=PENDING" style={{ fontSize: "0.8rem" }}>Xử lý ngay →</a>
        </div>

        <div className="panel" style={{ margin: 0, borderTop: "4px solid #3476C2" }}>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>Đang cắm & Đang giao</span>
          <div style={{ fontSize: "1.8rem", fontWeight: 700, color: "#3476C2", margin: "0.4rem 0" }}>
            {stats.processingOrders + stats.shippingOrders}
          </div>
          <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>{stats.processingOrders} chuẩn bị, {stats.shippingOrders} đang giao</span>
        </div>

        <div className="panel" style={{ margin: 0, borderTop: "4px solid #2A7E4B" }}>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>Đơn hàng hoàn tất</span>
          <div style={{ fontSize: "1.8rem", fontWeight: 700, color: "#2A7E4B", margin: "0.4rem 0" }}>
            {stats.completedOrders}
          </div>
          <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>Đã giao hoặc khách đã nhận</span>
        </div>

        <div className="panel" style={{ margin: 0, borderTop: "4px solid #C5221F" }}>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)" }}>Cảnh báo tồn kho thấp</span>
          <div style={{ fontSize: "1.8rem", fontWeight: 700, color: lowStockCount > 0 ? "#C5221F" : "#137333", margin: "0.4rem 0" }}>
            {lowStockCount}
          </div>
          <a href="/admin/ton-kho" style={{ fontSize: "0.8rem" }}>Kiểm tra kho →</a>
        </div>
      </div>

      {/* Recent Orders table */}
      <div className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h2>Đơn hàng gần đây</h2>
          <a href="/admin/don-hang" className="button button-sm button-secondary">Xem tất cả đơn hàng →</a>
        </div>

        {stats.recentOrders && stats.recentOrders.length > 0 ? (
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Người nhận</th>
                <th>Thời gian đặt</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {stats.recentOrders.map(o => (
                <tr key={o.id}>
                  <td><strong>{o.order_code}</strong></td>
                  <td>{o.recipient_name}</td>
                  <td>{new Date(o.created_at).toLocaleString("vi-VN")}</td>
                  <td><strong>{price(o.total)}</strong></td>
                  <td><span className={`status-pill status-${o.status}`}>{formatOrderStatus(o.status)}</span></td>
                  <td><a href={`/don-hang/${o.id}`} className="button button-sm button-secondary">Chi tiết</a></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p style={{ color: "var(--text-muted)" }}>Chưa có đơn hàng nào.</p>
        )}
      </div>
    </section>
  );
}


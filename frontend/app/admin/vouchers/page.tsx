import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { adminCreateVoucher } from "../../actions";
import { price } from "../../../lib/catalog";
import type { Voucher } from "../../../lib/checkout";

const base = process.env.GATEWAY_URL || "http://localhost:8080";

export default async function AdminVouchersPage() {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  let vouchers: Voucher[] = [];
  try {
    const res = await fetch(base + "/api/admin/vouchers", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
    if (res.ok) vouchers = await res.json();
  } catch {}

  return (
    <section>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "1.5rem" }}>
        <div>
          <p className="eyebrow">KHUYẾN MÃI & ƯU ĐÃI</p>
          <h1>Quản lý mã Voucher</h1>
        </div>
        <a href="/admin" className="button button-sm button-secondary">← Về Bảng điều khiển</a>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1.3fr 0.7fr", gap: "2rem", alignItems: "flex-start" }}>
        {/* Vouchers Table */}
        <div className="panel">
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Danh sách Voucher ({vouchers.length})</h2>

          <table className="data-table">
            <thead>
              <tr>
                <th>Mã code / Tên</th>
                <th>Mức giảm</th>
                <th>Đơn tối thiểu</th>
                <th>Lượt dùng</th>
                <th>Hạn dùng</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {vouchers.map(v => (
                <tr key={v.id}>
                  <td>
                    <strong style={{ color: "var(--primary)", letterSpacing: "0.05em" }}>{v.code}</strong><br />
                    <span style={{ fontSize: "0.82rem", color: "var(--text-muted)" }}>{v.name}</span>
                  </td>
                  <td>
                    {v.discount_type === "PERCENTAGE" ? (
                      <span><strong>{v.discount_value}%</strong> {v.max_discount && `(Tối đa ${price(v.max_discount)})`}</span>
                    ) : (
                      <strong>{price(v.discount_value)}</strong>
                    )}
                  </td>
                  <td>{price(v.min_order_value)}</td>
                  <td>
                    <strong>{v.used_count || 0}</strong> / {v.max_uses || "∞"}
                  </td>
                  <td style={{ fontSize: "0.82rem" }}>
                    {new Date(v.expires_at).toLocaleDateString("vi-VN")}
                  </td>
                  <td>
                    <span className={`status-pill ${v.status === "ACTIVE" ? "status-COMPLETED" : "status-CANCELLED"}`}>
                      {v.status || "ACTIVE"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Create Voucher Form */}
        <div className="panel">
          <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Tạo mã voucher mới</h2>
          <form action={adminCreateVoucher}>
            <label>
              Mã voucher (In hoa, không dấu)
              <input name="code" required placeholder="Ví dụ: FLORA83" style={{ textTransform: "uppercase" }} />
            </label>

            <label>
              Tên chương trình ưu đãi
              <input name="name" required placeholder="Ưu đãi mừng 8/3..." />
            </label>

            <label>
              Loại giảm giá
              <select name="discountType">
                <option value="PERCENTAGE">Phần trăm (%)</option>
                <option value="FIXED">Số tiền cố định (VND)</option>
              </select>
            </label>

            <label>
              Giá trị giảm (% hoặc số tiền)
              <input name="discountValue" type="number" required min={1} placeholder="10 (nếu là %) hoặc 50000 (nếu là tiền)" />
            </label>

            <label>
              Giảm tối đa (VND, áp dụng khi giảm theo %)
              <input name="maxDiscount" type="number" placeholder="50000" />
            </label>

            <label>
              Giá trị đơn hàng tối thiểu (VND)
              <input name="minOrderValue" type="number" defaultValue={200000} placeholder="200000" />
            </label>

            <label>
              Số lượt dùng tối đa
              <input name="maxUses" type="number" defaultValue={100} />
            </label>

            <button type="submit" style={{ width: "100%", marginTop: "1rem" }}>
              Tạo voucher
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}


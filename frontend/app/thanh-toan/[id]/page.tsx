import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { placeCod } from "../../actions";
import { gateway, type Quote } from "../../../lib/checkout";
import { price } from "../../../lib/catalog";

export default async function Confirmation({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ error?: string }>;
}) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  const { id } = await params;
  const query = await searchParams;

  let quote: Quote | null = null;
  if (/^[0-9a-f-]{36}$/i.test(id)) {
    try {
      const r = await fetch(gateway + "/api/checkout/quotes/" + id, {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
        signal: AbortSignal.timeout(10000),
      });
      if (r.ok) quote = (await r.json()) as Quote;
    } catch {}
  }

  return (
    <section>
      <p className="eyebrow">BƯỚC CUỐI CÙNG</p>
      <h1>Xác nhận đơn COD</h1>

      {query.error && (
        <p role="alert">
          {query.error} <a href="/don-hang">Xem đơn hàng đã tạo</a>
        </p>
      )}

      {!quote ? (
        <div className="panel" style={{ textAlign: "center", padding: "2.5rem" }}>
          <p>Báo giá không còn khả dụng hoặc đã hết thời hạn 10 phút.</p>
          <div style={{ marginTop: "1rem" }}>
            <a className="button" href="/thanh-toan">Lấy báo giá mới</a>
            <a className="button button-secondary" href="/don-hang" style={{ marginLeft: "0.8rem" }}>
              Kiểm tra đơn đã tạo
            </a>
          </div>
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "1.2fr 0.8fr", gap: "2rem", alignItems: "flex-start" }}>
          <div>
            <div className="panel">
              <h2 style={{ fontSize: "1.2rem", marginBottom: "0.8rem" }}>Thông tin người nhận</h2>
              <p><strong>Người nhận:</strong> {quote.recipientName} ({quote.recipientPhone})</p>
              <p><strong>Địa chỉ:</strong> {quote.address}</p>
              <p><strong>Lịch giao:</strong> {quote.deliveryDate} · Khung giờ: {quote.timeSlot}</p>
              {quote.cardMessage && (
                <div style={{ background: "#F5F2EB", padding: "0.8rem", borderRadius: "var(--radius-sm)", marginTop: "0.8rem" }}>
                  <strong>Lời nhắn thiệp:</strong> <em>"{quote.cardMessage}"</em>
                </div>
              )}
            </div>

            <div className="panel">
              <h2 style={{ fontSize: "1.2rem", marginBottom: "0.8rem" }}>Phương thức thanh toán</h2>
              <div style={{ padding: "0.8rem", border: "1px solid var(--primary)", borderRadius: "var(--radius-sm)", background: "rgba(49,91,69,0.04)", marginBottom: "0.8rem" }}>
                <label style={{ display: "flex", alignItems: "center", gap: "0.6rem", fontWeight: 700, margin: 0 }}>
                  <input type="radio" name="paymentMethod" defaultChecked readOnly style={{ width: "auto" }} />
                  Thanh toán khi nhận hàng (COD)
                </label>
                <p style={{ fontSize: "0.85rem", color: "var(--text-muted)", margin: "0.4rem 0 0 1.6rem" }}>
                  Thanh toán bằng tiền mặt trực tiếp cho nhân viên giao hoa khi nhận được sản phẩm tươi đẹp.
                </p>
              </div>

              <div style={{ padding: "0.8rem", border: "1px dashed var(--border)", borderRadius: "var(--radius-sm)", opacity: 0.85 }}>
                <span style={{ fontSize: "0.8rem", background: "#E8A598", color: "#FFF", padding: "0.15rem 0.5rem", borderRadius: "10px", fontWeight: 700, marginRight: "0.5rem" }}>
                  MÔI TRƯỜNG GIẢ LẬP
                </span>
                <span style={{ fontWeight: 600, fontSize: "0.95rem" }}>Ví điện tử MoMo Sandbox</span>
                <p style={{ fontSize: "0.82rem", color: "var(--text-muted)", margin: "0.3rem 0 0" }}>
                  Tích hợp module thanh toán giả lập không trừ tiền thật.
                </p>
              </div>
            </div>
          </div>

          <div className="panel">
            <h2 style={{ fontSize: "1.2rem", marginBottom: "1rem" }}>Chi tiết thanh toán</h2>

            <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem", marginBottom: "1rem" }}>
              {quote.items.map(i => (
                <div key={i.productId} style={{ display: "flex", justifyContent: "space-between", fontSize: "0.92rem" }}>
                  <span>{i.name} × {i.quantity}</span>
                  <strong>{price(i.lineTotal)}</strong>
                </div>
              ))}
            </div>

            <hr style={{ border: "none", borderTop: "1px solid var(--border)", margin: "0.8rem 0" }} />

            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.4rem" }}>
              <span>Tiền hàng:</span>
              <span>{price(quote.subtotal)}</span>
            </div>

            {quote.discountAmount !== undefined && quote.discountAmount > 0 && (
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.4rem", color: "#137333" }}>
                <span>Giảm giá ({quote.voucherCode || "Voucher"}):</span>
                <span>-{price(quote.discountAmount)}</span>
              </div>
            )}

            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.8rem" }}>
              <span>Phí giao:</span>
              <span>{price(quote.shippingFee)} {quote.shippingMode === "MOCK" && "(MOCK)"}</span>
            </div>

            <hr style={{ border: "none", borderTop: "1px solid var(--border)", margin: "0.8rem 0" }} />

            <div style={{ marginBottom: "1.2rem", fontSize: "1.15rem" }}>
              <p style={{ margin: 0 }}><strong style={{ color: "var(--primary)" }}>Tổng COD: {price(quote.total)}</strong></p>
            </div>

            <p style={{ fontSize: "0.8rem", color: "var(--text-muted)", marginBottom: "1.2rem" }}>
              Chưa thu tiền. Thanh toán khi nhận hàng. Hệ thống áp dụng khóa bảo vệ chống trùng đơn và tự động giữ hàng trong kho.
            </p>

            <form action={placeCod}>
              <input type="hidden" name="quoteId" value={quote.id} />
              <button type="submit" style={{ width: "100%", height: "46px" }}>
                Xác nhận đặt đơn COD
              </button>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { removeCart, setCart } from "../actions";
import { price, type Product } from "../../lib/catalog";

import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();
type Item = { item_id: string; quantity: number };

export default async function Cart({ searchParams }: { searchParams: Promise<{ error?: string; notice?: string }> }) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=Vui%20l%C3%B2ng%20%C4%91%C4%83ng%20nh%E1%BA%ADp%20%C4%91%E1%BB%83%20xem%20gi%E1%BB%8F%20h%C3%A0ng");

  const params = await searchParams;
  let items: Item[] | null = null;
  const products: Record<string, Product> = {};

  try {
    const response = await fetch(getBase() + "/api/cart", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
    if (response.status === 401) redirect("/tai-khoan?error=Phi%C3%AAn%20%C4%91%C3%A3%20h%E1%BA%BFt%20h%E1%BA%A1n");
    if (response.ok) {
      items = (await response.json()) as Item[];
      const details = await Promise.all(
        items.map(async item => {
          try {
            const r = await fetch(getBase() + "/api/products/" + item.item_id, {
              cache: "no-store",
              signal: AbortSignal.timeout(5000),
            });
            return r.ok ? ((await r.json()) as Product) : null;
          } catch {
            return null;
          }
        })
      );
      details.forEach(p => {
        if (p) products[p.id] = p;
      });
    }
  } catch (e) {
    if (e && typeof e === "object" && "digest" in e) throw e;
  }

  let subtotal = 0;
  if (items) {
    items.forEach(item => {
      const p = products[item.item_id];
      if (p) subtotal += p.price * item.quantity;
    });
  }

  return (
    <section>
      <p className="eyebrow">GIỎ HÀNG CỦA BẠN</p>
      <h1>Giỏ hoa của bạn</h1>

      {params.error && <p role="alert">{params.error}</p>}
      {params.notice && <p role="status">{params.notice}</p>}

      {items === null ? (
        <p role="alert">Không thể tải giỏ hàng. Vui lòng thử lại.</p>
      ) : items.length === 0 ? (
        <div className="panel" style={{ textAlign: "center", padding: "3rem 1.5rem" }}>
          <span style={{ fontSize: "3rem", display: "block", marginBottom: "1rem" }}>🧺</span>
          <h2>Giỏ hàng của bạn đang trống</h2>
          <p style={{ color: "var(--text-muted)", marginBottom: "1.5rem" }}>
            Hãy chọn những bó hoa tươi đẹp nhất để tặng người thân yêu.
          </p>
          <a className="button" href="/san-pham">Khám phá hoa ngay</a>
        </div>
      ) : (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 340px", gap: "2rem", alignItems: "flex-start" }}>
          {/* Cart items list */}
          <div>
            {items.map(item => {
              const p = products[item.item_id];
              const lineTotal = p ? p.price * item.quantity : 0;

              return (
                <article className="panel" key={item.item_id} style={{ display: "flex", gap: "1.2rem", alignItems: "center" }}>
                  <div style={{ width: "90px", height: "90px", borderRadius: "var(--radius-sm)", overflow: "hidden", background: "#EFE9E4", flexShrink: 0 }}>
                    {p?.image ? (
                      <img src={p.image} alt={p.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                    ) : (
                      <div style={{ width: "100%", height: "100%", display: "grid", placeItems: "center", fontSize: "2rem", color: "var(--accent-pink)" }}>✿</div>
                    )}
                  </div>

                  <div style={{ flex: 1 }}>
                    <h3 style={{ fontSize: "1.1rem", marginBottom: "0.2rem" }}>
                      <a href={`/san-pham/${p?.slug || item.item_id}`}>{p?.name || "Sản phẩm không còn bán"}</a>
                    </h3>
                    <p style={{ color: "var(--text-muted)", fontSize: "0.88rem", margin: 0 }}>
                      Đơn giá: {p ? price(p.price) : "—"}
                    </p>
                    <p style={{ color: "var(--primary)", fontWeight: 700, margin: "0.2rem 0" }}>
                      Thành tiền: {price(lineTotal)}
                    </p>
                  </div>

                  <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem", alignItems: "flex-end" }}>
                    <form action={setCart} style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
                      <input type="hidden" name="id" value={item.item_id} />
                      <label htmlFor={`qty-${item.item_id}`} style={{ marginBottom: 0, fontSize: "0.8rem" }}>SL:</label>
                      <input
                        id={`qty-${item.item_id}`}
                        type="number"
                        name="quantity"
                        min="1"
                        max="99"
                        defaultValue={item.quantity}
                        required
                        style={{ width: "65px", padding: "0.4rem" }}
                      />
                      <button type="submit" className="button button-sm button-secondary">Lưu</button>
                    </form>

                    <form action={removeCart}>
                      <input type="hidden" name="id" value={item.item_id} />
                      <button type="submit" className="button button-sm" style={{ background: "transparent", color: "#A83232", border: "none" }}>
                        Xóa
                      </button>
                    </form>
                  </div>
                </article>
              );
            })}
          </div>

          {/* Cart Summary */}
          <div className="panel" style={{ position: "sticky", top: "80px" }}>
            <h2 style={{ fontSize: "1.3rem", marginBottom: "1rem" }}>Tóm tắt đơn hàng</h2>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.6rem" }}>
              <span>Tạm tính tiền hàng:</span>
              <strong>{price(subtotal)}</strong>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "0.6rem", color: "var(--text-muted)", fontSize: "0.9rem" }}>
              <span>Phí giao hàng:</span>
              <span>Tính khi thanh toán</span>
            </div>
            <hr style={{ border: "none", borderTop: "1px solid var(--border)", margin: "1rem 0" }} />
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1.2rem", fontSize: "1.1rem" }}>
              <strong>Tổng cộng:</strong>
              <strong style={{ color: "var(--primary)" }}>{price(subtotal)}</strong>
            </div>

            <p style={{ fontSize: "0.8rem", color: "var(--text-muted)", marginBottom: "1rem" }}>
              Giá và số lượng tồn kho khả dụng sẽ được hệ thống kiểm tra và xác nhận lần cuối khi bạn đặt hàng.
            </p>

            <a className="button" href="/thanh-toan" style={{ width: "100%", textAlign: "center" }}>
              Tiếp tục thanh toán COD
            </a>

            <a href="/san-pham" className="button button-secondary" style={{ width: "100%", textAlign: "center", marginTop: "0.6rem" }}>
              Tiếp tục chọn hoa
            </a>
          </div>
        </div>
      )}
    </section>
  );
}

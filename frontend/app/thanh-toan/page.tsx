import { createQuote } from "../actions";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();

type Address = {
  id: string;
  recipient_name: string;
  recipient_phone: string;
  province: string;
  district: string;
  ward: string;
  address_detail: string;
  is_default: boolean;
};

export default async function Checkout({ searchParams }: { searchParams: Promise<{ error?: string }> }) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=Vui%20l%C3%B2ng%20%C4%91%C4%83ng%20nh%E1%BA%ADp%20%C4%91%E1%BB%83%20ti%E1%BA%BFp%20t%E1%BB%A5c%20thanh%20to%C3%A1n");

  const params = await searchParams;

  // Retrieve user's saved addresses from customer-service
  let addresses: Address[] = [];
  try {
    const res = await fetch(getBase() + "/api/customer/addresses", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(5000),
    });
    if (res.ok) addresses = await res.json();
  } catch {}

  const defaultAddr = addresses.find(a => a.is_default) || addresses[0];

  return (
    <section>
      <p className="eyebrow">ĐẶT HOA TRỰC TUYẾN</p>
      <h1>Thông tin giao hoa</h1>

      {params.error && <p role="alert">{params.error}</p>}

      <div style={{ display: "grid", gridTemplateColumns: "1.2fr 0.8fr", gap: "2rem", alignItems: "flex-start" }}>
        <form action={createQuote} className="panel">
          <h2 style={{ fontSize: "1.3rem", marginBottom: "1.2rem" }}>1. Thông tin người nhận & địa chỉ</h2>

          <label>
            Người nhận
            <input
              name="recipientName"
              required
              maxLength={120}
              autoComplete="name"
              defaultValue={defaultAddr?.recipient_name || ""}
              placeholder="Họ và tên người nhận hoa"
            />
          </label>

          <label>
            Số điện thoại
            <input
              name="recipientPhone"
              type="tel"
              required
              maxLength={20}
              autoComplete="tel"
              defaultValue={defaultAddr?.recipient_phone || ""}
              placeholder="Ví dụ: 0901234567"
            />
          </label>

          <label>
            Địa chỉ giao
            <input
              name="address"
              required
              minLength={5}
              maxLength={500}
              autoComplete="street-address"
              defaultValue={defaultAddr ? `${defaultAddr.address_detail}, ${defaultAddr.ward}, ${defaultAddr.district}, ${defaultAddr.province}` : ""}
              placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/TP"
            />
          </label>

          <h2 style={{ fontSize: "1.3rem", margin: "1.5rem 0 1rem" }}>2. Lịch giao & Lời nhắn</h2>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1rem" }}>
            <label>
              Ngày giao
              <input name="deliveryDate" type="date" required />
            </label>

            <label>
              Khung giờ đề nghị
              <select name="timeSlot">
                <option value="08:00–11:00">08:00 – 11:00 (Sáng sớm)</option>
                <option value="11:00–14:00">11:00 – 14:00 (Buổi trưa)</option>
                <option value="14:00–17:00">14:00 – 17:00 (Buổi chiều)</option>
                <option value="17:00–20:00">17:00 – 20:00 (Buổi tối)</option>
              </select>
            </label>
          </div>

          <label>
            Lời nhắn trên thiệp
            <textarea
              name="cardMessage"
              maxLength={1000}
              placeholder="Nhập nội dung bạn muốn FLORA viết tay nắn nót lên thiệp chúc mừng..."
            />
          </label>

          <h2 style={{ fontSize: "1.3rem", margin: "1.5rem 0 1rem" }}>3. Mã ưu đãi / Voucher</h2>

          <label>
            Mã voucher (nếu có)
            <input
              name="voucherCode"
              placeholder="Nhập mã FLORA10 hoặc FLORANEW để nhận ưu đãi"
              style={{ textTransform: "uppercase" }}
            />
          </label>
          <p style={{ fontSize: "0.82rem", color: "var(--text-muted)", marginTop: "0.2rem" }}>
            Mã gợi ý: <strong>FLORA10</strong> (giảm 10% tối đa 50k cho đơn từ 200k), <strong>FLORANEW</strong> (giảm 50k cho đơn từ 300k).
          </p>

          <div style={{ marginTop: "1.5rem", paddingTop: "1rem", borderTop: "1px solid var(--border)" }}>
            <p style={{ fontSize: "0.88rem", color: "var(--text-muted)", marginBottom: "1rem" }}>
              Khung giờ giao sẽ được Florist của FLORA gọi điện xác nhận sau khi tiếp nhận đơn. Bạn sẽ kiểm tra lại toàn bộ chi tiết và tổng tiền trước khi đặt đơn chính thức.
            </p>
            <button type="submit" style={{ width: "100%", height: "46px" }}>
              Xem báo giá
            </button>
          </div>
        </form>

        {/* Right side helper info */}
        <div>
          <div className="panel">
            <h3 style={{ fontSize: "1.1rem", marginBottom: "0.8rem" }}>Chính sách giao hoa FLORA</h3>
            <ul style={{ paddingLeft: "1.2rem", fontSize: "0.88rem", color: "var(--text-main)", lineHeight: 1.8 }}>
              <li>Giao đúng khung giờ bạn đã chọn.</li>
              <li>Chụp ảnh bó hoa trước khi giao để bạn duyệt mẫu.</li>
              <li>Tặng miễn phí thiệp chúc mừng thiết kế riêng.</li>
              <li>Phí giao hàng giả lập cố định: <strong>30.000đ</strong> (cho môi trường kiểm thử local).</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}

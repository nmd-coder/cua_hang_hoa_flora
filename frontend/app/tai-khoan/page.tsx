import { logout, updateProfile, addAddress, deleteAddress, setDefaultAddress } from "../actions";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { price } from "../../lib/catalog";

import { getGatewayUrl } from "../../lib/config";

const getBase = () => getGatewayUrl();

type UserProfile = {
  id: string;
  email: string;
  roles: string[];
};

type CustomerProfile = {
  full_name: string;
  avatar_url: string;
};

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

export default async function AccountPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string; notice?: string }>;
}) {
  const msg = await searchParams;
  const token = (await cookies()).get("flora_session")?.value;

  let user: UserProfile | null = null;
  let profile: CustomerProfile | null = null;
  let addresses: Address[] = [];
  let wishlistIds: string[] = [];

  if (token) {
    try {
      const meRes = await fetch(getBase() + "/api/auth/me", {
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
      });
      if (meRes.ok) user = await meRes.json();

      const [pRes, aRes, wRes] = await Promise.all([
        fetch(getBase() + "/api/customer/profile", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
        fetch(getBase() + "/api/customer/addresses", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
        fetch(getBase() + "/api/customer/wishlist", { headers: { Authorization: "Bearer " + token }, cache: "no-store" }),
      ]);

      if (pRes.ok) profile = await pRes.json();
      if (aRes.ok) addresses = await aRes.json();
      if (wRes.ok) wishlistIds = await wRes.json();
    } catch {}
  }

  if (!user) {
    const q = new URLSearchParams();
    if (msg.error) q.set("error", msg.error);
    if (msg.notice) q.set("notice", msg.notice);
    const qs = q.toString() ? `?${q.toString()}` : "";
    redirect(`/dang-nhap${qs}`);
  }

  return (
    <section>
      <p className="eyebrow">TÀI KHOẢN KHÁCH HÀNG</p>
      <h1>Xin chào, {profile?.full_name || user.email.split("@")[0]}</h1>

      {msg.error && <p role="alert">{msg.error}</p>}
      {msg.notice && <p role="status">{msg.notice}</p>}

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "2rem", alignItems: "flex-start" }}>
          <div>
            {/* User Profile */}
            <div className="panel">
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                <h2>Thông tin cá nhân</h2>
                <form action={logout}>
                  <button type="submit" className="button button-sm button-secondary">Đăng xuất</button>
                </form>
              </div>

              <p><strong>Email:</strong> {user.email}</p>
              <p><strong>Vai trò:</strong> {user.roles.join(", ")}</p>

              <hr style={{ border: "none", borderTop: "1px solid var(--border)", margin: "1rem 0" }} />

              <form action={updateProfile}>
                <label>
                  Họ và tên
                  <input name="fullName" defaultValue={profile?.full_name || ""} placeholder="Nhập họ và tên đầy đủ" />
                </label>
                <label>
                  Ảnh đại diện (URL)
                  <input name="avatarUrl" defaultValue={profile?.avatar_url || ""} placeholder="https://..." />
                </label>
                <button type="submit" style={{ marginTop: "0.6rem" }}>Cập nhật hồ sơ</button>
              </form>
            </div>

            {/* Wishlist Box */}
            <div id="yeu-thich" className="panel" style={{ borderLeft: "4px solid var(--accent-peach)" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                  <h3 style={{ margin: 0 }}>♥ Hoa yêu thích ({wishlistIds.length})</h3>
                  <p style={{ margin: "0.2rem 0 0", fontSize: "0.88rem", color: "var(--text-muted)" }}>
                    {wishlistIds.length > 0
                      ? `Bạn đã lưu ${wishlistIds.length} mẫu hoa vào danh sách mong muốn.`
                      : "Bạn chưa lưu mẫu hoa nào vào danh sách."}
                  </p>
                </div>
                <a href="/yeu-thich" className="button button-sm button-secondary">
                  Xem danh sách →
                </a>
              </div>
            </div>

            {/* Quick links for Staff & Admin */}
            {(user.roles.includes("STAFF") || user.roles.includes("ADMIN")) && (
              <div className="panel" style={{ background: "#F0F5FA", border: "1px solid #C2D5E8" }}>
                <h3 style={{ color: "#1D5288" }}>Khu vực làm việc</h3>
                <p style={{ fontSize: "0.9rem" }}>Bạn đang đăng nhập với tư cách nhân viên hoặc quản trị viên:</p>
                <div style={{ display: "flex", gap: "0.8rem", marginTop: "0.8rem" }}>
                  <a href="/nhan-vien" className="button button-sm">Giao diện Nhân viên</a>
                  {user.roles.includes("ADMIN") && (
                    <a href="/admin" className="button button-sm button-secondary">Giao diện Quản trị viên</a>
                  )}
                </div>
              </div>
            )}
          </div>

          <div>
            {/* Addresses */}
            <div className="panel">
              <h2>Sổ địa chỉ nhận hàng</h2>

              {addresses.length === 0 ? (
                <p style={{ color: "var(--text-muted)", fontSize: "0.9rem" }}>Chưa có địa chỉ nào được lưu.</p>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: "0.8rem", marginBottom: "1.2rem" }}>
                  {addresses.map(a => (
                    <div key={a.id} style={{ padding: "0.8rem", border: "1px solid var(--border)", borderRadius: "var(--radius-sm)", background: a.is_default ? "rgba(49,91,69,0.04)" : "#fff" }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                        <strong>{a.recipient_name} ({a.recipient_phone})</strong>
                        {a.is_default && <span className="status-pill status-COMPLETED">Mặc định</span>}
                      </div>
                      <p style={{ fontSize: "0.88rem", margin: "0.3rem 0" }}>
                        {a.address_detail}, {a.ward}, {a.district}, {a.province}
                      </p>
                      <div style={{ display: "flex", gap: "0.5rem", marginTop: "0.4rem" }}>
                        {!a.is_default && (
                          <form action={setDefaultAddress}>
                            <input type="hidden" name="id" value={a.id} />
                            <button type="submit" className="button button-sm button-secondary">Đặt làm mặc định</button>
                          </form>
                        )}
                        <form action={deleteAddress}>
                          <input type="hidden" name="id" value={a.id} />
                          <button type="submit" className="button button-sm" style={{ background: "transparent", color: "#A83232", border: "none" }}>
                            Xóa
                          </button>
                        </form>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <hr style={{ border: "none", borderTop: "1px solid var(--border)", margin: "1rem 0" }} />

              {/* Add Address Form */}
              <h3 style={{ fontSize: "1.05rem", marginBottom: "0.8rem" }}>Thêm địa chỉ mới</h3>
              <form action={addAddress}>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.8rem" }}>
                  <label>
                    Người nhận
                    <input name="recipientName" required placeholder="Họ và tên" />
                  </label>
                  <label>
                    Số điện thoại
                    <input name="recipientPhone" required placeholder="0901234567" />
                  </label>
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "0.8rem" }}>
                  <label>
                    Tỉnh / Thành
                    <input name="province" required defaultValue="TP. Hồ Chí Minh" />
                  </label>
                  <label>
                    Quận / Huyện
                    <input name="district" required placeholder="Quận 1" />
                  </label>
                  <label>
                    Phường / Xã
                    <input name="ward" required placeholder="Phường Bến Nghé" />
                  </label>
                </div>
                <label>
                  Địa chỉ cụ thể
                  <input name="addressDetail" required placeholder="Số nhà, tên đường..." />
                </label>
                <label style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.4rem", fontWeight: "normal" }}>
                  <input type="checkbox" name="isDefault" style={{ width: "auto" }} />
                  Đặt làm địa chỉ giao hàng mặc định
                </label>
                <button type="submit" style={{ marginTop: "0.8rem" }}>Lưu địa chỉ</button>
              </form>
            </div>
          </div>
        </div>
    </section>
  );
}

import { login } from "../actions";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import Link from "next/link";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string; notice?: string }>;
}) {
  const msg = await searchParams;
  const token = (await cookies()).get("flora_session")?.value;

  if (token) {
    redirect("/tai-khoan");
  }

  return (
    <section className="auth-page-container">
      <div className="auth-card">
        <div className="auth-header">
          <p className="eyebrow">CHÀO MỪNG TRỞ LẠI</p>
          <h1>Đăng nhập</h1>
          <p className="auth-subtitle">
            Đăng nhập để theo dõi đơn hàng, quản lý danh sách yêu thích và nhận ưu đãi riêng.
          </p>
        </div>

        {msg.error && (
          <div className="alert-message alert-error" role="alert">
            <span>⚠️</span> {msg.error}
          </div>
        )}
        {msg.notice && (
          <div className="alert-message alert-success" role="status">
            <span>✅</span> {msg.notice}
          </div>
        )}

        <form action={login} className="auth-form">
          <label>
            Email
            <input
              type="email"
              name="email"
              required
              maxLength={254}
              placeholder="name@example.com"
              autoComplete="username"
            />
          </label>

          <label>
            Mật khẩu
            <input
              type="password"
              name="password"
              required
              minLength={10}
              placeholder="••••••••••"
              autoComplete="current-password"
            />
          </label>

          <button type="submit" className="button button-primary auth-submit-btn">
            Đăng nhập
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Chưa có tài khoản?{" "}
            <Link href="/dang-ky" className="auth-link">
              <strong>Đăng ký tài khoản mới →</strong>
            </Link>
          </p>
          <div className="auth-divider"></div>
          <Link href="/" className="auth-back-link">
            ← Quay lại trang chủ
          </Link>
        </div>
      </div>
    </section>
  );
}


import { register } from "../actions";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import Link from "next/link";

export default async function RegisterPage({
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
          <p className="eyebrow">THÀNH VIÊN FLORA</p>
          <h1>Đăng ký</h1>
          <p className="auth-subtitle">
            Tạo tài khoản ngay hôm nay để trải nghiệm dịch vụ hoa tươi nghệ thuật chuẩn cao cấp.
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

        <form action={register} className="auth-form">
          <label>
            Họ và tên
            <input
              name="fullName"
              placeholder="Ví dụ: Nguyễn Văn An"
              autoComplete="name"
            />
          </label>

          <label>
            Số điện thoại
            <input
              name="phone"
              type="tel"
              placeholder="0901234567"
              autoComplete="tel"
            />
          </label>

          <label>
            Email
            <input
              type="email"
              name="email"
              required
              maxLength={254}
              placeholder="name@example.com"
              autoComplete="email"
            />
          </label>

          <label>
            Mật khẩu (tối thiểu 10 ký tự)
            <input
              type="password"
              name="password"
              required
              minLength={10}
              placeholder="••••••••••"
              autoComplete="new-password"
            />
          </label>

          <button type="submit" className="button button-primary auth-submit-btn">
            Tạo tài khoản
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Đã có tài khoản FLORA?{" "}
            <Link href="/dang-nhap" className="auth-link">
              <strong>Đăng nhập ngay →</strong>
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


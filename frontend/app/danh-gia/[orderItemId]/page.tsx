import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { submitReview } from "../../actions";

export default async function ReviewPage({
  params,
  searchParams,
}: {
  params: Promise<{ orderItemId: string }>;
  searchParams: Promise<{ productId?: string; name?: string; error?: string }>;
}) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  const { orderItemId } = await params;
  const query = await searchParams;

  return (
    <section style={{ maxWidth: "600px", margin: "0 auto" }}>
      <p className="eyebrow">ĐÁNH GIÁ SẢN PHẨM</p>
      <h1>Chia sẻ cảm nhận của bạn</h1>

      {query.error && <p role="alert">{query.error}</p>}

      <div className="panel">
        <h2 style={{ fontSize: "1.2rem", marginBottom: "0.4rem" }}>
          {query.name ? decodeURIComponent(query.name) : "Sản phẩm hoa tươi"}
        </h2>
        <p style={{ color: "var(--text-muted)", fontSize: "0.88rem", marginBottom: "1.5rem" }}>
          Đánh giá chỉ khả dụng sau khi bạn đã nhận được hoa thành công. Mỗi sản phẩm trong đơn được đánh giá tối đa 1 lần.
        </p>

        <form action={submitReview}>
          <input type="hidden" name="orderItemId" value={orderItemId} />
          <input type="hidden" name="productId" value={query.productId || ""} />

          <label htmlFor="rating-select">Đánh giá số sao (từ 1 đến 5 sao)</label>
          <select id="rating-select" name="rating" defaultValue={5} style={{ marginBottom: "1.2rem" }}>
            <option value="5">★★★★★ (5 sao - Tuyệt vời, hoa rất tươi và đẹp)</option>
            <option value="4">★★★★☆ (4 sao - Hài lòng, dịch vụ tốt)</option>
            <option value="3">★★★☆☆ (3 sao - Bình thường)</option>
            <option value="2">★★☆☆☆ (2 sao - Cần cải thiện)</option>
            <option value="1">★☆☆☆☆ (1 sao - Không hài lòng)</option>
          </select>

          <label htmlFor="review-comment">Nội dung đánh giá chi tiết</label>
          <textarea
            id="review-comment"
            name="comment"
            required
            minLength={5}
            maxLength={1000}
            placeholder="Chia sẻ về độ tươi của hoa, cách gói, thời gian giao hoặc cảm xúc của người nhận..."
            style={{ minHeight: "120px", marginBottom: "1.5rem" }}
          />

          <div style={{ display: "flex", gap: "1rem" }}>
            <button type="submit" style={{ flex: 1 }}>Gửi đánh giá</button>
            <a href="/don-hang" className="button button-secondary">Quay lại</a>
          </div>
        </form>
      </div>
    </section>
  );
}


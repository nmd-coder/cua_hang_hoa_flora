"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { getGatewayUrl } from "../lib/config";

const getBase = () => getGatewayUrl();
const error = (message: string) => encodeURIComponent(message);
const notice = (message: string) => encodeURIComponent(message);

async function authRequest(path: string, body: Record<string, any>) {
  try {
    const res = await fetch(getBase() + path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
    return res;
  } catch (err) {
    console.error("authRequest fetch failed:", path, err);
    return null;
  }
}

export async function register(form: FormData) {
  const email = String(form.get("email") || "");
  const password = String(form.get("password") || "");
  const phone = String(form.get("phone") || "");
  const fullName = String(form.get("fullName") || "");

  const result = await authRequest("/api/auth/register", { email, password, phone });
  if (!result?.ok) {
    console.error("REGISTER ERROR:", result?.status, email);
    redirect("/dang-ky?error=" + error(result?.status === 409 ? "Email đã được sử dụng." : "Không thể đăng ký. Kiểm tra email và mật khẩu từ 10 ký tự."));
  }

  // If full name provided, auto-login and update profile
  if (fullName) {
    try {
      const loginRes = await authRequest("/api/auth/login", { email, password });
      if (loginRes?.ok) {
        const data = await loginRes.json() as { accessToken: string; expiresAt: string };
        (await cookies()).set("flora_session", data.accessToken, {
          httpOnly: true,
          sameSite: "strict",
          secure: process.env.NODE_ENV === "production" && process.env.COOKIE_SECURE === "true",
          path: "/",
          expires: new Date(data.expiresAt),
        });
        await fetch(getBase() + "/api/customer/profile", {
          method: "PUT",
          headers: { "Content-Type": "application/json", Authorization: "Bearer " + data.accessToken },
          body: JSON.stringify({ fullName, avatarUrl: null }),
        });
        redirect("/tai-khoan?notice=" + notice("Đăng ký tài khoản thành công!"));
      }
    } catch {
      // Fallback
    }
  }

  redirect("/dang-nhap?notice=" + notice("Đăng ký thành công. Hãy đăng nhập."));
}

export async function login(form: FormData) {
  const email = String(form.get("email") || "");
  const password = String(form.get("password") || "");
  const result = await authRequest("/api/auth/login", { email, password });
  if (!result?.ok) {
    redirect("/dang-nhap?error=" + error("Đăng nhập không thành công. Vui lòng kiểm tra email và mật khẩu."));
  }
  const data = await result.json() as { accessToken: string; expiresAt: string };
  if (!/^[A-Za-z0-9_-]{43}$/.test(data.accessToken)) {
    redirect("/dang-nhap?error=" + error("Không thể tạo phiên đăng nhập."));
  }
  (await cookies()).set("flora_session", data.accessToken, {
    httpOnly: true,
    sameSite: "strict",
    secure: process.env.NODE_ENV === "production" && process.env.COOKIE_SECURE === "true",
    path: "/",
    expires: new Date(data.expiresAt),
  });

  // Check roles to redirect appropriately
  try {
    const meRes = await fetch(getBase() + "/api/auth/me", {
      headers: { Authorization: "Bearer " + data.accessToken },
      cache: "no-store",
    });
    if (meRes.ok) {
      const profile = await meRes.json() as { roles?: string[] };
      if (profile.roles?.includes("ADMIN")) {
        redirect("/admin");
      }
      if (profile.roles?.includes("STAFF")) {
        redirect("/nhan-vien");
      }
    }
  } catch {
    // Continue to cart
  }

  redirect("/gio-hang");
}

export async function logout() {
  const jar = await cookies();
  const token = jar.get("flora_session")?.value;
  if (token) {
    try {
      await fetch(getBase() + "/api/auth/logout", {
        method: "POST",
        headers: { Authorization: "Bearer " + token },
        cache: "no-store",
        signal: AbortSignal.timeout(5000),
      });
    } catch {
      /* Local cookie is still removed */
    }
  }
  jar.delete("flora_session");
  redirect("/dang-nhap");
}

export async function addToCart(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=" + error("Vui lòng đăng nhập để thêm hoa vào giỏ hàng."));
  const id = String(form.get("id") || "");
  const quantity = Number(form.get("quantity") || 1);
  if (!/^[0-9a-f-]{36}$/i.test(id) || quantity < 1 || quantity > 99) {
    redirect("/san-pham?error=" + error("Sản phẩm hoặc số lượng không hợp lệ."));
  }

  // Get current cart items to accumulate quantity if item already exists
  let currentQty = 0;
  try {
    const cartRes = await fetch(getBase() + "/api/cart", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
    if (cartRes.ok) {
      const items = await cartRes.json() as { item_id: string; quantity: number }[];
      const found = items.find(i => i.item_id === id);
      if (found) currentQty = found.quantity;
    }
  } catch {}

  const newQty = Math.min(99, currentQty + quantity);
  try {
    await fetch(getBase() + "/api/cart/items/" + id, {
      method: "PUT",
      headers: { "Content-Type": "application/json", Authorization: "Bearer " + token },
      body: JSON.stringify({ quantity: newQty }),
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
  } catch {}

  redirect("/gio-hang");
}

export async function setCart(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=" + error("Vui lòng đăng nhập trước."));
  const id = String(form.get("id") || "");
  const quantity = Number(form.get("quantity"));
  if (!/^[0-9a-f-]{36}$/i.test(id) || !Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
    redirect("/gio-hang?error=" + error("Số lượng không hợp lệ."));
  }
  let ok = false;
  try {
    const r = await fetch(getBase() + "/api/cart/items/" + id, {
      method: "PUT",
      headers: { "Content-Type": "application/json", Authorization: "Bearer " + token },
      body: JSON.stringify({ quantity }),
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
    ok = r.ok;
  } catch {}
  if (!ok) redirect("/gio-hang?error=" + error("Không thể cập nhật giỏ hàng."));
  redirect("/gio-hang");
}

export async function removeCart(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  if (!/^[0-9a-f-]{36}$/i.test(id)) redirect("/gio-hang?error=" + error("Sản phẩm không hợp lệ."));
  let ok = false;
  try {
    ok = (await fetch(getBase() + "/api/cart/items/" + id, {
      method: "DELETE",
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    })).ok;
  } catch {}
  if (!ok) redirect("/gio-hang?error=" + error("Không thể xóa sản phẩm."));
  redirect("/gio-hang");
}

export async function createQuote(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  let quoteId: string | null = null;
  let quoteError = "";

  try {
    const cart = await fetch(getBase() + "/api/cart", {
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(10000),
    });
    if (cart.ok) {
      const lines = await cart.json() as { item_id: string; quantity: number }[];
      const voucherCode = String(form.get("voucherCode") || "").trim() || undefined;
      const payload = {
        items: lines.map(i => ({ productId: i.item_id, quantity: i.quantity })),
        recipientName: String(form.get("recipientName") || ""),
        recipientPhone: String(form.get("recipientPhone") || ""),
        address: String(form.get("address") || ""),
        deliveryDate: String(form.get("deliveryDate") || ""),
        timeSlot: String(form.get("timeSlot") || ""),
        cardMessage: String(form.get("cardMessage") || ""),
        voucherCode,
      };

      const r = await fetch(getBase() + "/api/checkout/quotes", {
        method: "POST",
        headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        cache: "no-store",
        signal: AbortSignal.timeout(20000),
      });

      if (r.ok) {
        quoteId = (await r.json() as { id: string }).id;
      } else {
        const errJson = await r.json().catch(() => ({}));
        quoteError = errJson.message || "Không thể tạo báo giá.";
      }
    }
  } catch {}

  if (!quoteId) {
    redirect("/thanh-toan?error=" + error(quoteError || "Không thể báo giá. Kiểm tra giỏ hàng, thông tin giao hàng và phiên đăng nhập."));
  }
  redirect("/thanh-toan/" + quoteId);
}

export async function placeCod(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("quoteId") || "");
  if (!/^[0-9a-f-]{36}$/i.test(id)) redirect("/thanh-toan");
  let orderId: string | null = null;
  let conflict = false;

  try {
    const r = await fetch(getBase() + "/api/orders", {
      method: "POST",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json",
        "Idempotency-Key": "cod-" + id,
      },
      body: JSON.stringify({ quoteId: id }),
      cache: "no-store",
      signal: AbortSignal.timeout(20000),
    });
    conflict = r.status === 409;
    if (r.ok) orderId = (await r.json() as { id: string }).id;
  } catch {}

  if (orderId) redirect("/don-hang/" + orderId);
  redirect("/thanh-toan/" + id + "?error=" + error(conflict ? "Không thể đặt đơn: giá hoặc tồn kho đã thay đổi. Vui lòng lấy báo giá mới." : "Chưa xác nhận được kết quả. Kiểm tra danh sách đơn hoặc thử lại bằng nút xác nhận này."));
}

export async function cancelOrder(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  if (!/^[0-9a-f-]{36}$/i.test(id)) redirect("/don-hang");
  let ok = false;
  try {
    ok = (await fetch(getBase() + "/api/orders/" + id + "/cancel", {
      method: "POST",
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
      signal: AbortSignal.timeout(15000),
    })).ok;
  } catch {}
  if (!ok) redirect("/don-hang/" + id + "?error=" + error("Chưa xác nhận được việc hủy. Kiểm tra trạng thái hoặc thử lại."));
  redirect("/don-hang/" + id);
}

export async function confirmReceived(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  if (!/^[0-9a-f-]{36}$/i.test(id)) redirect("/don-hang");
  let ok = false;
  try {
    ok = (await fetch(getBase() + "/api/orders/" + id + "/status", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ status: "RECEIVED" }),
      cache: "no-store",
      signal: AbortSignal.timeout(15000),
    })).ok;
  } catch {}
  if (!ok) redirect("/don-hang/" + id + "?error=" + error("Không xác nhận được. Kiểm tra trạng thái đơn và thử lại."));
  redirect("/don-hang/" + id);
}

// --- Customer Account Actions ---
export async function updateProfile(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const fullName = String(form.get("fullName") || "").trim();
  const avatarUrl = String(form.get("avatarUrl") || "").trim() || null;
  try {
    await fetch(getBase() + "/api/customer/profile", {
      method: "PUT",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ fullName, avatarUrl }),
      cache: "no-store",
    });
  } catch {}
  redirect("/tai-khoan?notice=" + notice("Cập nhật thông tin cá nhân thành công"));
}

export async function addAddress(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const payload = {
    recipientName: String(form.get("recipientName") || ""),
    recipientPhone: String(form.get("recipientPhone") || ""),
    province: String(form.get("province") || ""),
    district: String(form.get("district") || ""),
    ward: String(form.get("ward") || ""),
    addressDetail: String(form.get("addressDetail") || ""),
    isDefault: form.get("isDefault") === "on",
  };
  try {
    await fetch(getBase() + "/api/customer/addresses", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });
  } catch {}
  redirect("/tai-khoan?notice=" + notice("Đã thêm địa chỉ nhận hàng"));
}

export async function setDefaultAddress(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  try {
    await fetch(getBase() + "/api/customer/addresses/" + id + "/default", {
      method: "PUT",
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
  } catch {}
  redirect("/tai-khoan");
}

export async function deleteAddress(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  try {
    await fetch(getBase() + "/api/customer/addresses/" + id, {
      method: "DELETE",
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
  } catch {}
  redirect("/tai-khoan");
}

export async function toggleWishlist(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan?error=" + error("Vui lòng đăng nhập để lưu vào yêu thích"));
  const productId = String(form.get("productId") || "");
  const current = form.get("isFavorited") === "true";
  try {
    await fetch(getBase() + "/api/customer/wishlist/" + productId, {
      method: current ? "DELETE" : "POST",
      headers: { Authorization: "Bearer " + token },
      cache: "no-store",
    });
  } catch {}
  redirect("/tai-khoan#yeu-thich");
}

export async function submitReview(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const orderItemId = String(form.get("orderItemId") || "");
  const productId = String(form.get("productId") || "");
  const rating = Number(form.get("rating") || 5);
  const comment = String(form.get("comment") || "");

  let ok = false;
  let errMsg = "";
  try {
    const res = await fetch(getBase() + "/api/reviews", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ orderItemId, productId, rating, comment }),
      cache: "no-store",
    });
    ok = res.ok;
    if (!ok) {
      const err = await res.json().catch(() => ({}));
      errMsg = err.message || "Không thể gửi đánh giá.";
    }
  } catch {}

  if (!ok) {
    redirect("/don-hang?error=" + error(errMsg || "Đánh giá không thành công"));
  }
  redirect("/san-pham/" + productId + "?notice=" + notice("Cảm ơn bạn đã gửi đánh giá!"));
}

// --- Staff & Admin Actions ---
export async function staffUpdateOrderStatus(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const orderId = String(form.get("orderId") || "");
  const status = String(form.get("status") || "");
  const reason = String(form.get("reason") || "");

  try {
    await fetch(getBase() + "/api/admin/orders/" + orderId + "/status", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ status, reason }),
      cache: "no-store",
    });
  } catch {}
  redirect("/nhan-vien");
}

export async function adminUpdateOrderStatus(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const orderId = String(form.get("orderId") || "");
  const status = String(form.get("status") || "");
  const reason = String(form.get("reason") || "");

  try {
    await fetch(getBase() + "/api/admin/orders/" + orderId + "/status", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ status, reason }),
      cache: "no-store",
    });
  } catch {}
  redirect("/admin/don-hang");
}

export async function adminAdjustStock(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const itemId = String(form.get("itemId") || "");
  const changeQty = Number(form.get("changeQty") || 0);
  const reason = String(form.get("reason") || "Điều chỉnh tồn kho định kỳ");

  try {
    await fetch(getBase() + "/api/inventory/adjust", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ itemId, changeQty, reason, actorId: null }),
      cache: "no-store",
    });
  } catch {}
  redirect("/admin/ton-kho");
}

export async function adminToggleProductStatus(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");
  const id = String(form.get("id") || "");
  const currentStatus = String(form.get("currentStatus") || "ACTIVE");
  const nextStatus = currentStatus === "ACTIVE" ? "HIDDEN" : "ACTIVE";

  try {
    await fetch(getBase() + "/api/admin/products/" + id + "/status", {
      method: "PATCH",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify({ status: nextStatus }),
      cache: "no-store",
    });
  } catch {}
  redirect("/admin/san-pham");
}

export async function adminCreateProduct(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  const payload = {
    name: String(form.get("name") || ""),
    sku: String(form.get("sku") || ""),
    description: String(form.get("description") || ""),
    price: Number(form.get("price") || 0),
    imageUrl: String(form.get("imageUrl") || ""),
    categoryId: String(form.get("categoryId") || "") || undefined,
  };

  try {
    await fetch(getBase() + "/api/admin/products", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });
  } catch {}
  redirect("/admin/san-pham");
}

export async function adminCreateVoucher(form: FormData) {
  const token = (await cookies()).get("flora_session")?.value;
  if (!token) redirect("/tai-khoan");

  const payload = {
    code: String(form.get("code") || "").trim().toUpperCase(),
    name: String(form.get("name") || "").trim(),
    discountType: String(form.get("discountType") || "PERCENTAGE"),
    discountValue: Number(form.get("discountValue") || 10),
    maxDiscount: form.get("maxDiscount") ? Number(form.get("maxDiscount")) : undefined,
    minOrderValue: Number(form.get("minOrderValue") || 0),
    maxUses: Number(form.get("maxUses") || 100),
  };

  try {
    await fetch(getBase() + "/api/admin/vouchers", {
      method: "POST",
      headers: { Authorization: "Bearer " + token, "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });
  } catch {}
  redirect("/admin/vouchers");
}

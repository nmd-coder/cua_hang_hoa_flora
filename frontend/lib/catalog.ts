export type Product = {
  id: string;
  sku: string;
  name: string;
  slug: string;
  description: string | null;
  price: number;
  original_price?: number | null;
  status: string;
  image: string | null;
  category_name?: string | null;
  category_slug?: string | null;
  weight_g?: number;
  length_cm?: number;
  width_cm?: number;
  height_cm?: number;
  images?: { id: string; url: string; alt_text: string; sort_order: number; is_primary: boolean }[];
  occasions?: { id: string; name: string; slug: string }[];
  flower_types?: { id: string; name: string; slug: string }[];
  colors?: { id: string; name: string; hex_code: string }[];
};

export type Category = { id: string; name: string; slug: string; description: string | null; status: string };
export type Occasion = { id: string; name: string; slug: string };
export type FlowerType = { id: string; name: string; slug: string };
export type Color = { id: string; name: string; hex_code: string };

import { getGatewayUrl } from "./config";

const getBase = () => getGatewayUrl();

export async function fetchCatalog(options: {
  q?: string;
  category?: string;
  occasion?: string;
  flowerType?: string;
  color?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
  page?: number;
  size?: number;
} = {}): Promise<Product[]> {
  const url = new URL("/api/products", getBase());
  if (options.q) url.searchParams.set("q", options.q);
  if (options.category) url.searchParams.set("category", options.category);
  if (options.occasion) url.searchParams.set("occasion", options.occasion);
  if (options.flowerType) url.searchParams.set("flower_type", options.flowerType);
  if (options.color) url.searchParams.set("color", options.color);
  if (options.minPrice !== undefined) url.searchParams.set("min_price", options.minPrice.toString());
  if (options.maxPrice !== undefined) url.searchParams.set("max_price", options.maxPrice.toString());
  if (options.sort) url.searchParams.set("sort", options.sort);
  if (options.page !== undefined) url.searchParams.set("page", options.page.toString());
  const fetchSize = options.size !== undefined ? options.size : 100;
  url.searchParams.set("size", fetchSize.toString());

  const response = await fetch(url, { cache: "no-store", signal: AbortSignal.timeout(15000) });
  if (!response.ok) throw new Error("Không thể tải danh mục sản phẩm");
  return response.json() as Promise<Product[]>;
}

export async function products(q = "", size = 100): Promise<Product[]> {
  return fetchCatalog({ q, size });
}

export async function productDetail(idOrSlug: string): Promise<Product | null> {
  const url = new URL("/api/products/" + idOrSlug, getBase());
  try {
    const response = await fetch(url, { cache: "no-store", signal: AbortSignal.timeout(15000) });
    if (!response.ok) return null;
    return response.json() as Promise<Product>;
  } catch {
    return null;
  }
}

export async function getCategories(): Promise<Category[]> {
  try {
    const res = await fetch(getBase() + "/api/categories", { cache: "no-store", signal: AbortSignal.timeout(15000) });
    return res.ok ? await res.json() : [];
  } catch {
    return [];
  }
}

export async function getOccasions(): Promise<Occasion[]> {
  try {
    const res = await fetch(getBase() + "/api/occasions", { cache: "no-store", signal: AbortSignal.timeout(15000) });
    return res.ok ? await res.json() : [];
  } catch {
    return [];
  }
}

export async function getFlowerTypes(): Promise<FlowerType[]> {
  try {
    const res = await fetch(getBase() + "/api/flower-types", { cache: "no-store", signal: AbortSignal.timeout(15000) });
    return res.ok ? await res.json() : [];
  } catch {
    return [];
  }
}

export async function getColors(): Promise<Color[]> {
  try {
    const res = await fetch(getBase() + "/api/colors", { cache: "no-store", signal: AbortSignal.timeout(15000) });
    return res.ok ? await res.json() : [];
  } catch {
    return [];
  }
}

export function price(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND", maximumFractionDigits: 0 }).format(value);
}

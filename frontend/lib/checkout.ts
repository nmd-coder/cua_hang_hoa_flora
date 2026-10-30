export type Quote = {
  id: string;
  expiresAt: string;
  items: {
    productId: string;
    sku: string;
    name: string;
    image?: string;
    unitPrice: number;
    quantity: number;
    lineTotal: number;
  }[];
  recipientName: string;
  recipientPhone: string;
  address: string;
  deliveryDate: string;
  timeSlot: string;
  cardMessage: string;
  subtotal: number;
  discountAmount?: number;
  shippingFee: number;
  total: number;
  voucherCode?: string | null;
  currency: string;
  shippingMode: string;
};

export type Voucher = {
  id: string;
  code: string;
  name: string;
  discount_type: "PERCENTAGE" | "FIXED";
  discount_value: number;
  max_discount?: number;
  min_order_value: number;
  starts_at: string;
  expires_at: string;
  max_uses?: number;
  used_count?: number;
  status?: string;
};

import { getGatewayUrl } from "./config";

export const gateway = getGatewayUrl();

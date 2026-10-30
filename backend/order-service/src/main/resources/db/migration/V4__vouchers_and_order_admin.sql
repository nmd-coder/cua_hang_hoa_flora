-- Vouchers and Promotions module

CREATE TABLE IF NOT EXISTS vouchers (
    id UUID PRIMARY KEY,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    discount_type TEXT NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    discount_value NUMERIC(19,2) NOT NULL CHECK (discount_value > 0),
    max_discount NUMERIC(19,2),
    min_order_value NUMERIC(19,2) NOT NULL DEFAULT 0,
    starts_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    max_uses INTEGER NOT NULL DEFAULT 100,
    used_count INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS voucher_redemptions (
    id UUID PRIMARY KEY,
    voucher_id UUID NOT NULL REFERENCES vouchers(id),
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    discount_applied NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(voucher_id, order_id)
);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS voucher_code TEXT;

-- Seed Sample Vouchers
INSERT INTO vouchers (id, code, name, discount_type, discount_value, max_discount, min_order_value, starts_at, expires_at, max_uses, used_count, status)
VALUES
('60000000-0000-0000-0000-000000000001', 'FLORA10', 'Giảm 10% mừng bạn mới', 'PERCENTAGE', 10, 50000, 200000, now() - INTERVAL '1 day', now() + INTERVAL '365 days', 500, 0, 'ACTIVE'),
('60000000-0000-0000-0000-000000000002', 'FLORANEW', 'Ưu đãi 50.000đ cho đơn từ 300.000đ', 'FIXED', 50000, 50000, 300000, now() - INTERVAL '1 day', now() + INTERVAL '365 days', 200, 0, 'ACTIVE'),
('60000000-0000-0000-0000-000000000003', 'FLORALOVE', 'Tri ân tình yêu giảm 100.000đ', 'FIXED', 100000, 100000, 800000, now() - INTERVAL '1 day', now() + INTERVAL '365 days', 100, 0, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;


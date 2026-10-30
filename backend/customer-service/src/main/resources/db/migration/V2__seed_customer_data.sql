-- Seed default customer profiles and reviews

INSERT INTO customer_profiles (user_id, full_name, avatar_url) VALUES
('00000000-0000-0000-0000-000000000001', 'Nguyễn Thị Ánh Dương', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80'),
('00000000-0000-0000-0000-000000000002', 'Trần Văn Nhân (Nhân viên FLORA)', NULL),
('00000000-0000-0000-0000-000000000003', 'Quản trị viên FLORA', NULL)
ON CONFLICT (user_id) DO NOTHING;

-- Seed default address for customer
INSERT INTO addresses (id, user_id, recipient_name, recipient_phone, province, district, ward, address_detail, is_default)
VALUES
('70000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Nguyễn Thị Ánh Dương', '0901234567', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Đường Lê Lợi', true)
ON CONFLICT (id) DO NOTHING;

-- Seed sample reviews for popular flowers
INSERT INTO reviews (id, order_item_id, user_id, product_id, rating, comment, created_at)
VALUES
('80000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 5, 'Hoa rất tươi và đẹp, giao đúng hẹn 9h sáng. Người yêu mình nhận được khen nức nở! Sẽ ủng hộ shop dài dài.', now() - INTERVAL '5 days'),
('80000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 5, 'Bó hồng đỏ Ohara thơm ngát, gói giấy rất sang trọng. Thiệp viết tay nắn nót rất có tâm.', now() - INTERVAL '3 days'),
('80000000-0000-0000-0000-000000000003', '90000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', 5, 'Hướng dương nở đều, phối baby trắng rất sáng bừng căn phòng tốt nghiệp.', now() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;


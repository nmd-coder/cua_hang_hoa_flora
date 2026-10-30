-- Synthetic local development catalog; no personal data.
INSERT INTO categories(id,name,slug,status) VALUES ('00000000-0000-0000-0000-000000000001','Hoa bó','hoa-bo','ACTIVE');
INSERT INTO products(id,category_id,sku,name,slug,description,price,status) VALUES
('10000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','FL-001','Nắng dịu dàng','nang-diu-dang','Bó hoa dành tặng những khoảnh khắc ấm áp.',350000,'ACTIVE'),
('10000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000001','FL-002','Lời yêu thương','loi-yeu-thuong','Một lời nhắn gửi bằng hoa.',420000,'ACTIVE'),
('10000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000001','FL-003','Ngày thật đẹp','ngay-that-dep','Hoa tươi cho một ngày đặc biệt.',290000,'ACTIVE');
-- Fix product images for Chậu Lan Bạch Ngọc and Mây Trắng Bồng Bềnh
UPDATE catalog.product_images
SET url = '/images/products/chau-lan-bach-ngoc.jpg'
WHERE product_id = '10000000-0000-0000-0000-000000000010';

UPDATE catalog.product_images
SET url = '/images/products/may-trang-bong-benh.jpg'
WHERE product_id = '10000000-0000-0000-0000-000000000011';


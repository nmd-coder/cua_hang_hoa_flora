-- V6: Add original_price column and set regular prices for all products
SET client_encoding = 'UTF8';

ALTER TABLE catalog.products ADD COLUMN IF NOT EXISTS original_price numeric(19, 2);

-- Set original prices (giá niêm yết / giá gốc) for all 20 products:
UPDATE catalog.products SET original_price = 450000.00 WHERE sku = 'FL-001';
UPDATE catalog.products SET original_price = 500000.00 WHERE sku = 'FL-002';
UPDATE catalog.products SET original_price = 350000.00 WHERE sku = 'FL-003';
UPDATE catalog.products SET original_price = 620000.00 WHERE sku = 'FL-004';
UPDATE catalog.products SET original_price = 1150000.00 WHERE sku = 'FL-005';
UPDATE catalog.products SET original_price = 750000.00 WHERE sku = 'FL-006';
UPDATE catalog.products SET original_price = 820000.00 WHERE sku = 'FL-007';
UPDATE catalog.products SET original_price = 1750000.00 WHERE sku = 'FL-008';
UPDATE catalog.products SET original_price = 1950000.00 WHERE sku = 'FL-009';
UPDATE catalog.products SET original_price = 2200000.00 WHERE sku = 'FL-010';
UPDATE catalog.products SET original_price = 650000.00 WHERE sku = 'FL-011';
UPDATE catalog.products SET original_price = 550000.00 WHERE sku = 'FL-012';
UPDATE catalog.products SET original_price = 1050000.00 WHERE sku = 'FL-013';
UPDATE catalog.products SET original_price = 1500000.00 WHERE sku = 'FL-014';
UPDATE catalog.products SET original_price = 950000.00 WHERE sku = 'FL-015';
UPDATE catalog.products SET original_price = 780000.00 WHERE sku = 'FL-016';
UPDATE catalog.products SET original_price = 1150000.00 WHERE sku = 'FL-017';
UPDATE catalog.products SET original_price = 2850000.00 WHERE sku = 'FL-018';
UPDATE catalog.products SET original_price = 890000.00 WHERE sku = 'FL-019';
UPDATE catalog.products SET original_price = 800000.00 WHERE sku = 'FL-020';

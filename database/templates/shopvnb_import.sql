-- Generated from the supplied ShopVNB JSON, not a live stock/price feed.
-- Run after Flyway V12. The client MUST stop on the first error (never --force).
-- The Node import tool closes a failed connection, rolling back the transaction.
-- Initial DH stock is 20 per new product; reruns preserve admin commercial values.
SET NAMES utf8mb4;
SET @dh_initial_stock = COALESCE(@dh_initial_stock, 20);
SET @dh_import_lock = GET_LOCK(CONCAT(DATABASE(), ':shopvnb-import'), 30);
-- Invalid JSON deliberately raises an error if the precondition is not met.
SET @dh_import_guard = IF(COALESCE(@dh_import_lock, 0) = 1
    AND @dh_initial_stock BETWEEN 0 AND 2147483647
    AND @dh_initial_stock = FLOOR(@dh_initial_stock), 1,
    JSON_EXTRACT('Cannot lock catalog or invalid initial stock', '$'));
SET @dh_import_guard = IF(EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '12' AND success = 1), 1,
    JSON_EXTRACT('Run backend Flyway migrations through V12 first', '$'));
SET @dh_source = CAST({{SOURCE_JSON}} AS JSON);

START TRANSACTION;
INSERT INTO categories (category_name, slug, status, created_at, updated_at)
SELECT DISTINCT s.category_name, s.category_slug, 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM {{STAGE_TABLE}} s
WHERE NOT EXISTS (SELECT 1 FROM categories c WHERE LOWER(TRIM(c.category_name)) = LOWER(s.category_name) OR c.slug = s.category_slug);
INSERT INTO brands (brand_name, slug, status, created_at, updated_at)
SELECT DISTINCT s.brand_name, s.brand_slug, 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM {{STAGE_TABLE}} s
WHERE NOT EXISTS (SELECT 1 FROM brands b WHERE LOWER(TRIM(b.brand_name)) = LOWER(s.brand_name) OR b.slug = s.brand_slug);
SET @dh_map = (
    SELECT JSON_ARRAYAGG(JSON_OBJECT('source_key', s.source_key,
        'brand_id', (SELECT MIN(b.brand_id) FROM brands b WHERE LOWER(TRIM(b.brand_name)) = LOWER(s.brand_name) OR b.slug = s.brand_slug),
        'category_id', (SELECT MIN(c.category_id) FROM categories c WHERE LOWER(TRIM(c.category_name)) = LOWER(s.category_name) OR c.slug = s.category_slug)))
    FROM {{STAGE_TABLE}} s
);
SET @dh_map = (
    SELECT JSON_ARRAYAGG(JSON_OBJECT('source_key', m.source_key, 'brand_id', m.brand_id, 'category_id', m.category_id,
        'product_id', COALESCE(
            (SELECT i.product_id FROM catalog_import_sources i WHERE i.source_key = s.source_key),
            (SELECT p.product_id FROM products p WHERE p.slug = s.product_slug),
            (SELECT MAX(p.product_id) FROM products p WHERE p.brand_id = m.brand_id AND p.category_id = m.category_id
                AND p.product_name = s.product_name HAVING COUNT(*) = 1))))
    FROM {{MAP_TABLE}} m JOIN {{STAGE_TABLE}} s ON s.source_key = m.source_key
);
INSERT INTO products (category_id, brand_id, product_name, slug, description, price, sale_price,
    stock, thumbnail_url, status, promotion_published, created_at, updated_at)
SELECT m.category_id, m.brand_id, s.product_name, s.product_slug, s.description, COALESCE(s.price, 0), NULL,
    @dh_initial_stock, s.thumbnail_url, IF(s.price > 0, 'ACTIVE', 'INACTIVE'), FALSE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM {{STAGE_TABLE}} s JOIN {{MAP_TABLE}} m ON m.source_key = s.source_key
WHERE m.product_id IS NULL;
SET @dh_map = (
    SELECT JSON_ARRAYAGG(JSON_OBJECT('source_key', m.source_key, 'brand_id', m.brand_id, 'category_id', m.category_id,
        'product_id', COALESCE(m.product_id, p.product_id)))
    FROM {{MAP_TABLE}} m JOIN {{STAGE_TABLE}} s ON s.source_key = m.source_key
    LEFT JOIN products p ON p.slug = s.product_slug
);
-- Fill only missing descriptive data. Commercial values belong to the admin.
UPDATE products p JOIN {{MAP_TABLE}} m ON m.product_id = p.product_id
JOIN {{STAGE_TABLE}} s ON s.source_key = m.source_key
SET p.description = IF(p.description IS NULL OR TRIM(p.description) = '', s.description, p.description),
    p.thumbnail_url = IF(p.thumbnail_url IS NULL OR TRIM(p.thumbnail_url) = '', s.thumbnail_url, p.thumbnail_url);
-- Source colors and sizes become selectable options. A missing dimension is represented
-- internally by a sentinel so the storefront only renders the attribute that exists.
INSERT INTO product_variants (product_id, sku, color_name, size_label, stock, status, created_at, updated_at)
SELECT product_id,
    CONCAT('IMP-', SUBSTRING(SHA2(CONCAT(source_key, '|', color_name, '|', size_label), 256), 1, 32)),
    color_name, size_label, 20,
    IF(product_status = 'ACTIVE', 'ACTIVE', 'OUT_OF_STOCK'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM (
    SELECT m.product_id, s.source_key, p.status AS product_status, colors.color_name, sizes.size_label
    FROM {{MAP_TABLE}} m
    JOIN {{STAGE_TABLE}} s ON s.source_key = m.source_key
    JOIN products p ON p.product_id = m.product_id
    CROSS JOIN JSON_TABLE(
        CASE WHEN JSON_LENGTH(JSON_EXTRACT(s.raw_record, '$.mau_sac')) > 0
            THEN JSON_EXTRACT(s.raw_record, '$.mau_sac') ELSE JSON_ARRAY('Mặc định') END,
        '$[*]' COLUMNS (color_name VARCHAR(80) PATH '$')) colors
    CROSS JOIN JSON_TABLE(
        CASE WHEN JSON_LENGTH(JSON_EXTRACT(s.raw_record, '$.kich_co')) > 0
            THEN JSON_EXTRACT(s.raw_record, '$.kich_co') ELSE JSON_ARRAY('Tiêu chuẩn') END,
        '$[*]' COLUMNS (size_label VARCHAR(50) PATH '$')) sizes
    WHERE JSON_LENGTH(JSON_EXTRACT(s.raw_record, '$.mau_sac')) > 0
       OR JSON_LENGTH(JSON_EXTRACT(s.raw_record, '$.kich_co')) > 0
) prepared
WHERE NOT EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = prepared.product_id);
UPDATE products p JOIN (
    SELECT product_id, SUM(stock) AS total_stock FROM product_variants GROUP BY product_id
) v ON v.product_id = p.product_id
SET p.stock = v.total_stock,
    p.status = IF(p.status = 'INACTIVE', 'INACTIVE', IF(v.total_stock > 0, 'ACTIVE', 'OUT_OF_STOCK'));
UPDATE catalog_import_sources i JOIN {{STAGE_TABLE}} s ON s.source_key = i.source_key
SET i.raw_record = s.raw_record, i.source_url = s.source_url, i.source_code = s.source_code, i.imported_at = UTC_TIMESTAMP(6);
INSERT INTO catalog_import_sources (source_key, source_name, source_url, source_code, product_id, raw_record, imported_at)
SELECT s.source_key, 'shopvnb.com', s.source_url, s.source_code, m.product_id, s.raw_record, UTC_TIMESTAMP(6)
FROM {{STAGE_TABLE}} s JOIN {{MAP_TABLE}} m ON m.source_key = s.source_key
WHERE NOT EXISTS (SELECT 1 FROM catalog_import_sources i WHERE i.source_key = s.source_key);
INSERT INTO product_images (product_id, image_url, sort_order, is_primary, created_at)
SELECT m.product_id, source_image.image_url, source_image.position - 1,
    source_image.position = 1 AND NOT EXISTS (
        SELECT 1 FROM product_images i WHERE i.product_id = m.product_id AND i.is_primary = TRUE), UTC_TIMESTAMP(6)
FROM {{MAP_TABLE}} m JOIN {{STAGE_TABLE}} s ON s.source_key = m.source_key
JOIN JSON_TABLE(s.raw_record, '$.anh_san_pham[*]' COLUMNS (
    position FOR ORDINALITY, image_url VARCHAR(500) PATH '$')) source_image
WHERE NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = m.product_id
        AND i.is_primary = TRUE AND i.image_url <> s.thumbnail_url)
    AND NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = m.product_id AND i.image_url = source_image.image_url);
COMMIT;
SELECT RELEASE_LOCK(CONCAT(DATABASE(), ':shopvnb-import')) AS import_lock_released;
SELECT COUNT(*) AS imported_products, SUM(p.status = 'ACTIVE') AS active_products,
    SUM(p.status = 'INACTIVE') AS inactive_products, SUM(p.stock) AS current_stock
FROM products p JOIN {{MAP_TABLE}} m ON m.product_id = p.product_id;
SET @dh_source = NULL, @dh_map = NULL, @dh_import_guard = NULL, @dh_initial_stock = NULL, @dh_import_lock = NULL;

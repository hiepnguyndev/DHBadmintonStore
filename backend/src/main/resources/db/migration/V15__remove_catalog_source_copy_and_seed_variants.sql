-- Catalog provenance remains in catalog_import_sources; do not expose it in product copy.
UPDATE products
SET description = TRIM(SUBSTRING_INDEX(description, '\nNguồn dữ liệu:', 1))
WHERE description LIKE '%Nguồn dữ liệu:%';

-- Give imported products selectable colors and sizes while preserving their total stock.
-- The sentinel values allow products that have only one attribute to expose only that control.
INSERT INTO product_variants (product_id, sku, color_name, size_label, stock, status, created_at, updated_at)
SELECT product_id,
    CONCAT('IMP-', SUBSTRING(SHA2(CONCAT(source_key, '|', color_name, '|', size_label), 256), 1, 32)),
    color_name, size_label, option_stock,
    IF(option_stock > 0 AND product_status = 'ACTIVE', 'ACTIVE', 'OUT_OF_STOCK'), UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM (
    SELECT options.*, FLOOR(product_stock / option_count)
        + IF(option_position <= MOD(product_stock, option_count), 1, 0) AS option_stock
    FROM (
        SELECT i.product_id, i.source_key, p.stock AS product_stock, p.status AS product_status,
            colors.color_name, sizes.size_label,
            COUNT(*) OVER (PARTITION BY i.product_id) AS option_count,
            ROW_NUMBER() OVER (PARTITION BY i.product_id ORDER BY colors.color_name, sizes.size_label) AS option_position
        FROM catalog_import_sources i
        JOIN products p ON p.product_id = i.product_id
        CROSS JOIN JSON_TABLE(
            CASE WHEN JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.mau_sac')) > 0
                THEN JSON_EXTRACT(i.raw_record, '$.mau_sac') ELSE JSON_ARRAY('Mặc định') END,
            '$[*]' COLUMNS (color_name VARCHAR(80) PATH '$')) colors
        CROSS JOIN JSON_TABLE(
            CASE WHEN JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.kich_co')) > 0
                THEN JSON_EXTRACT(i.raw_record, '$.kich_co') ELSE JSON_ARRAY('Tiêu chuẩn') END,
            '$[*]' COLUMNS (size_label VARCHAR(50) PATH '$')) sizes
        WHERE JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.mau_sac')) > 0
           OR JSON_LENGTH(JSON_EXTRACT(i.raw_record, '$.kich_co')) > 0
    ) options
) prepared
WHERE NOT EXISTS (SELECT 1 FROM product_variants v WHERE v.product_id = prepared.product_id);

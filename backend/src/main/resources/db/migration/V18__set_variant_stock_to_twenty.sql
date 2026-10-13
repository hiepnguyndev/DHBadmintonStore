-- Each selectable color/size combination starts with twenty units.
UPDATE product_variants
SET stock = 20,
    status = IF(status = 'INACTIVE', 'INACTIVE', 'ACTIVE');

-- Product inventory is the aggregate of its color/size variants.
UPDATE products p JOIN (
    SELECT product_id, SUM(stock) AS total_stock
    FROM product_variants
    GROUP BY product_id
) v ON v.product_id = p.product_id
SET p.stock = v.total_stock,
    p.status = IF(p.status = 'INACTIVE', 'INACTIVE', IF(v.total_stock > 0, 'ACTIVE', 'OUT_OF_STOCK'));

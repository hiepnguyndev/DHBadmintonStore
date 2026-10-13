-- Cleanup only: remove the three fictional demo rackets created by the former seed.
-- Products with order history are retained as inactive with zero stock so order history stays valid.
USE badminton_ecommerce;
START TRANSACTION;

DELETE FROM cart_items
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));
DELETE FROM favorites
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));
DELETE FROM recommendation_logs
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));
DELETE FROM product_variants
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));
DELETE FROM product_images
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));
DELETE FROM racket_specifications
WHERE product_id IN (SELECT product_id FROM products WHERE slug IN ('demo-easy-start','demo-all-round','demo-attack'));

DELETE p FROM products p
WHERE p.slug IN ('demo-easy-start','demo-all-round','demo-attack')
  AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.product_id = p.product_id);
UPDATE products p
SET p.stock = 0, p.status = 'INACTIVE'
WHERE p.slug IN ('demo-easy-start','demo-all-round','demo-attack')
  AND EXISTS (SELECT 1 FROM order_items oi WHERE oi.product_id = p.product_id);

DELETE FROM categories
WHERE slug = 'demo-rackets'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.category_id = categories.category_id);
DELETE FROM brands
WHERE slug = 'dh-demo'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.brand_id = brands.brand_id);
COMMIT;
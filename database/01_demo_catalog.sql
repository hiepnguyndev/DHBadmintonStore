-- OPTIONAL LOCAL DEMO ONLY. Run after Spring Boot has applied Flyway V1-V4.
-- Fictional products and specifications; these are not manufacturer claims.
USE badminton_ecommerce;
START TRANSACTION;
INSERT INTO categories (category_name,slug,description,status,created_at,updated_at)
VALUES ('Vợt cầu lông Demo','demo-rackets','Dữ liệu minh họa local','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE slug=VALUES(slug);
INSERT INTO brands (brand_name,slug,description,status,created_at,updated_at)
VALUES ('DH Demo','dh-demo','Thương hiệu giả lập cho đồ án','ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE slug=VALUES(slug);
SET @demo_category=(SELECT category_id FROM categories WHERE slug='demo-rackets');
SET @demo_brand=(SELECT brand_id FROM brands WHERE slug='dh-demo');
INSERT INTO products (category_id,brand_id,product_name,slug,description,price,stock,status,created_at,updated_at) VALUES
(@demo_category,@demo_brand,'Demo Easy Start 5U','demo-easy-start','Vợt minh họa cho người mới chơi; không phải thông số thương mại.',650000,20,'ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)),
(@demo_category,@demo_brand,'Demo All Round 4U','demo-all-round','Vợt minh họa lối chơi toàn diện.',1350000,15,'ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)),
(@demo_category,@demo_brand,'Demo Attack 3U','demo-attack','Vợt minh họa lối chơi tấn công nâng cao.',2300000,8,'ACTIVE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE slug=VALUES(slug);
INSERT INTO racket_specifications (product_id,weight_class,balance_type,shaft_flexibility,play_style,skill_level,created_at,updated_at)
SELECT product_id,'5U','HEAD_LIGHT','FLEXIBLE','DEFENSE','BEGINNER',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6) FROM products WHERE slug='demo-easy-start'
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id);
INSERT INTO racket_specifications (product_id,weight_class,balance_type,shaft_flexibility,play_style,skill_level,created_at,updated_at)
SELECT product_id,'4U','EVEN_BALANCE','MEDIUM','BALANCED','INTERMEDIATE',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6) FROM products WHERE slug='demo-all-round'
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id);
INSERT INTO racket_specifications (product_id,weight_class,balance_type,shaft_flexibility,play_style,skill_level,created_at,updated_at)
SELECT product_id,'3U','HEAD_HEAVY','STIFF','ATTACK','ADVANCED',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6) FROM products WHERE slug='demo-attack'
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id);
COMMIT;

INSERT INTO recommendation_weights(skill_weight,play_style_weight,wrist_weight,weight_class_weight,flex_weight,budget_weight,is_active,created_at,updated_at)
VALUES (20,25,15,15,10,15,TRUE,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));
CREATE INDEX idx_product_name ON products(product_name);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_listing_status ON used_listings(status);
CREATE INDEX idx_listing_price ON used_listings(selling_price);
CREATE INDEX idx_market_status ON marketplace_orders(order_status);
CREATE INDEX idx_order_status ON orders(order_status);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type,entity_id,created_at);

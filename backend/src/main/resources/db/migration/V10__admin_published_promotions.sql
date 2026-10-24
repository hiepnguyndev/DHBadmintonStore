ALTER TABLE products
    ADD COLUMN promotion_published BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_products_promotion_status ON products (promotion_published, status);

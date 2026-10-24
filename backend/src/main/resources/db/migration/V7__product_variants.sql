CREATE TABLE product_variants (
  variant_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  sku VARCHAR(100) NOT NULL,
  color VARCHAR(80) NOT NULL,
  size VARCHAR(50) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES products(product_id),
  CONSTRAINT uq_product_variants_sku UNIQUE (sku),
  CONSTRAINT uq_product_variants_option UNIQUE (product_id, color, size),
  CONSTRAINT chk_product_variants_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

ALTER TABLE cart_items DROP FOREIGN KEY cart_items_ibfk_1;
ALTER TABLE cart_items DROP INDEX cart_id;
ALTER TABLE cart_items ADD COLUMN variant_id BIGINT NULL;
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id);
ALTER TABLE cart_items ADD UNIQUE KEY uq_cart_variant (cart_id, variant_id);
ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id);

ALTER TABLE order_items ADD COLUMN variant_id BIGINT NULL;
ALTER TABLE order_items ADD COLUMN variant_sku VARCHAR(100) NULL;
ALTER TABLE order_items ADD COLUMN variant_color VARCHAR(80) NULL;
ALTER TABLE order_items ADD COLUMN variant_size VARCHAR(50) NULL;
ALTER TABLE order_items ADD COLUMN legacy_stock_rebased BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id);
UPDATE order_items SET legacy_stock_rebased = TRUE;

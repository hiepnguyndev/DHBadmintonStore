CREATE TABLE catalog_import_sources (
    source_key CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    source_name VARCHAR(80) NOT NULL,
    source_url VARCHAR(768) NOT NULL,
    source_code VARCHAR(80) NULL,
    product_id BIGINT NOT NULL,
    raw_record JSON NOT NULL,
    imported_at DATETIME(6) NOT NULL,
    PRIMARY KEY (source_key),
    CONSTRAINT uk_catalog_import_product UNIQUE (product_id),
    CONSTRAINT fk_catalog_import_product FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB;

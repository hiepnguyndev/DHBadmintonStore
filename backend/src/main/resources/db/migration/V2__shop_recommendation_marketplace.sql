CREATE TABLE categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE (slug)
) ENGINE=InnoDB;

CREATE TABLE brands (
    brand_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    logo_url VARCHAR(500),
    status VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE (slug)
) ENGINE=InnoDB;

CREATE TABLE products (
    product_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    sale_price DECIMAL(15,2),
    stock INT NOT NULL,
    thumbnail_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    UNIQUE (slug),
    CHECK (stock >= 0)
) ENGINE=InnoDB;

CREATE TABLE product_images (
    image_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL,
    is_primary BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB;

CREATE TABLE racket_specifications (
    specification_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    weight_class VARCHAR(10) NOT NULL,
    balance_type VARCHAR(30) NOT NULL,
    balance_point_mm INT,
    shaft_flexibility VARCHAR(30) NOT NULL,
    max_tension_lbs DECIMAL(5,2),
    play_style VARCHAR(30) NOT NULL,
    skill_level VARCHAR(30) NOT NULL,
    grip_size VARCHAR(20),
    racket_length_mm INT,
    material VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE (product_id)
) ENGINE=InnoDB;

CREATE TABLE carts (
    cart_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (user_id)
) ENGINE=InnoDB;

CREATE TABLE cart_items (
    cart_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE (cart_id, product_id),
    CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_code VARCHAR(50) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    subtotal_amount DECIMAL(15,2) NOT NULL,
    shipping_fee DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    note TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (order_code)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    order_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_image VARCHAR(500),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    CHECK (quantity > 0)
) ENGINE=InnoDB;

CREATE TABLE reviews (
    review_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment TEXT,
    status VARCHAR(20),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id),
    UNIQUE (user_id, order_item_id),
    CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE favorites (
    favorite_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    UNIQUE (user_id, product_id)
) ENGINE=InnoDB;

CREATE TABLE player_profiles (
    profile_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_level VARCHAR(30) NOT NULL,
    play_style VARCHAR(30) NOT NULL,
    play_type VARCHAR(30) NOT NULL,
    wrist_strength VARCHAR(30) NOT NULL,
    preferred_weight VARCHAR(10) NOT NULL,
    min_budget DECIMAL(15,2) NOT NULL,
    max_budget DECIMAL(15,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (user_id)
) ENGINE=InnoDB;

CREATE TABLE recommendation_weights (
    weight_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_weight DECIMAL(5,2) NOT NULL,
    play_style_weight DECIMAL(5,2) NOT NULL,
    wrist_weight DECIMAL(5,2) NOT NULL,
    weight_class_weight DECIMAL(5,2) NOT NULL,
    flex_weight DECIMAL(5,2) NOT NULL,
    budget_weight DECIMAL(5,2) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE recommendation_logs (
    log_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    match_score DECIMAL(5,2) NOT NULL,
    reason_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB;

CREATE TABLE used_listings (
    listing_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    listing_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    model VARCHAR(150) NOT NULL,
    description TEXT,
    original_price DECIMAL(15,2) NOT NULL,
    selling_price DECIMAL(15,2) NOT NULL,
    purchase_date DATE,
    usage_months INT NOT NULL,
    condition_grade VARCHAR(10) NOT NULL,
    condition_score DECIMAL(4,2),
    warranty_status VARCHAR(30) NOT NULL,
    warranty_until DATE,
    status VARCHAR(30) NOT NULL,
    view_count INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    UNIQUE (listing_code)
) ENGINE=InnoDB;

CREATE TABLE used_racket_specifications (
    used_spec_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    weight_class VARCHAR(10) NOT NULL,
    grip_size VARCHAR(20),
    string_model VARCHAR(100),
    string_tension_lbs DECIMAL(5,2),
    frame_condition VARCHAR(50) NOT NULL,
    paint_condition VARCHAR(50) NOT NULL,
    grommet_condition VARCHAR(50) NOT NULL,
    handle_condition VARCHAR(50) NOT NULL,
    repair_history TEXT,
    serial_number VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES used_listings(listing_id),
    UNIQUE (listing_id)
) ENGINE=InnoDB;

CREATE TABLE used_listing_images (
    image_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    image_type VARCHAR(30) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES used_listings(listing_id)
) ENGINE=InnoDB;

CREATE TABLE valuations (
    valuation_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    reference_price DECIMAL(15,2) NOT NULL,
    age_factor DECIMAL(5,3) NOT NULL,
    condition_factor DECIMAL(5,3) NOT NULL,
    warranty_factor DECIMAL(5,3) NOT NULL,
    accessory_factor DECIMAL(5,3) NOT NULL,
    repair_factor DECIMAL(5,3) NOT NULL,
    demand_factor DECIMAL(5,3) NOT NULL,
    estimated_price DECIMAL(15,2) NOT NULL,
    suggested_min_price DECIMAL(15,2) NOT NULL,
    suggested_max_price DECIMAL(15,2) NOT NULL,
    valuation_method VARCHAR(30) NOT NULL,
    condition_score DECIMAL(4,2),
    algorithm_version VARCHAR(50) NOT NULL,
    note TEXT,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES used_listings(listing_id)
) ENGINE=InnoDB;

CREATE TABLE inspection_requests (
    inspection_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    inspector_id BIGINT,
    inspection_code VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6),
    completed_at DATETIME(6),
    note TEXT,
    FOREIGN KEY (listing_id) REFERENCES used_listings(listing_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (inspector_id) REFERENCES users(user_id),
    UNIQUE (inspection_code)
) ENGINE=InnoDB;

CREATE TABLE inspection_reports (
    report_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    inspection_id BIGINT NOT NULL,
    model_verified BOOLEAN NOT NULL,
    serial_verified BOOLEAN NOT NULL,
    frame_condition VARCHAR(50) NOT NULL,
    paint_condition VARCHAR(50) NOT NULL,
    grommet_condition VARCHAR(50) NOT NULL,
    handle_condition VARCHAR(50) NOT NULL,
    crack_detected BOOLEAN NOT NULL,
    repair_detected BOOLEAN NOT NULL,
    overall_score DECIMAL(4,2) NOT NULL,
    recommended_min_price DECIMAL(15,2) NOT NULL,
    recommended_max_price DECIMAL(15,2) NOT NULL,
    notes TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (inspection_id) REFERENCES inspection_requests(inspection_id),
    UNIQUE (inspection_id)
) ENGINE=InnoDB;

CREATE TABLE marketplace_orders (
    market_order_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(50) NOT NULL,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    item_price DECIMAL(15,2) NOT NULL,
    shipping_fee DECIMAL(15,2) NOT NULL,
    platform_fee DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    cancelled_by_type VARCHAR(30),
    cancellation_reason VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (listing_id) REFERENCES used_listings(listing_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    UNIQUE (order_code)
) ENGINE=InnoDB;

CREATE TABLE escrow_transactions (
    escrow_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    market_order_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_reference VARCHAR(255),
    paid_at DATETIME(6),
    held_at DATETIME(6),
    released_at DATETIME(6),
    refunded_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (market_order_id) REFERENCES marketplace_orders(market_order_id),
    UNIQUE (market_order_id)
) ENGINE=InnoDB;

CREATE TABLE shipments (
    shipment_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    market_order_id BIGINT NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    tracking_code VARCHAR(100) NOT NULL,
    shipping_status VARCHAR(30) NOT NULL,
    shipped_at DATETIME(6),
    delivered_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    FOREIGN KEY (market_order_id) REFERENCES marketplace_orders(market_order_id),
    UNIQUE (market_order_id)
) ENGINE=InnoDB;

CREATE TABLE disputes (
    dispute_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    market_order_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    resolution VARCHAR(50),
    resolution_note TEXT,
    resolved_by BIGINT,
    resolved_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (market_order_id) REFERENCES marketplace_orders(market_order_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (resolved_by) REFERENCES users(user_id),
    UNIQUE (market_order_id)
) ENGINE=InnoDB;

CREATE TABLE dispute_evidences (
    evidence_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dispute_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(30) NOT NULL,
    description TEXT,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (dispute_id) REFERENCES disputes(dispute_id),
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE seller_ratings (
    rating_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    market_order_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    rating TINYINT NOT NULL,
    comment TEXT,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (market_order_id) REFERENCES marketplace_orders(market_order_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    UNIQUE (market_order_id),
    CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    notification_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    reference_type VARCHAR(50) NOT NULL,
    reference_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    audit_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    old_data JSON,
    new_data JSON,
    ip_address VARCHAR(50),
    created_at DATETIME(6) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

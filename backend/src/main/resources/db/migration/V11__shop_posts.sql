CREATE TABLE shop_posts (
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(400) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    cover_image_url VARCHAR(500) NULL,
    published_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (post_id),
    CONSTRAINT chk_shop_posts_category CHECK (category IN ('NEWS', 'COMMUNITY')),
    CONSTRAINT chk_shop_posts_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    INDEX idx_shop_posts_publication (status, published_at, post_id)
);

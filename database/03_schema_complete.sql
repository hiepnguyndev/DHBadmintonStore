-- Generated from a clean database migrated by Flyway V1-V13.
-- MySQL 8.0+. Select an EMPTY database first. Never run this to upgrade an existing database.
-- Includes real Flyway checksums and default recommendation weights; no accounts or passwords.
SET NAMES utf8mb4;
DELIMITER $$
CREATE PROCEDURE dh_install_schema_v13()
BEGIN
DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    SET FOREIGN_KEY_CHECKS = 1;
    RESIGNAL;
END;
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE()) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Database must be empty. Existing installations: start backend, then run 04_shopvnb_200.sql';
END IF;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(100) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province` varchar(100) NOT NULL,
  `district` varchar(100) NOT NULL,
  `ward` varchar(100) NOT NULL,
  `detail_address` varchar(255) NOT NULL,
  `is_default` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`address_id`),
  KEY `idx_addresses_user` (`user_id`),
  CONSTRAINT `fk_addresses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `audit_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `action` varchar(100) NOT NULL,
  `entity_type` varchar(100) NOT NULL,
  `entity_id` bigint NOT NULL,
  `old_data` json DEFAULT NULL,
  `new_data` json DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`audit_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_audit_entity` (`entity_type`,`entity_id`,`created_at`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brands` (
  `brand_id` bigint NOT NULL AUTO_INCREMENT,
  `brand_name` varchar(100) NOT NULL,
  `slug` varchar(100) NOT NULL,
  `description` text,
  `logo_url` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `slug` (`slug`),
  CONSTRAINT `ck_brand_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'INACTIVE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`cart_item_id`),
  UNIQUE KEY `uq_cart_variant` (`cart_id`,`variant_id`),
  KEY `product_id` (`product_id`),
  KEY `fk_cart_items_variant` (`variant_id`),
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `fk_cart_items_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`),
  CONSTRAINT `fk_cart_items_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`),
  CONSTRAINT `cart_items_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `carts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `catalog_import_sources` (
  `source_key` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `source_name` varchar(80) NOT NULL,
  `source_url` varchar(768) NOT NULL,
  `source_code` varchar(80) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `raw_record` json NOT NULL,
  `imported_at` datetime(6) NOT NULL,
  PRIMARY KEY (`source_key`),
  UNIQUE KEY `uk_catalog_import_product` (`product_id`),
  CONSTRAINT `fk_catalog_import_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) NOT NULL,
  `slug` varchar(100) NOT NULL,
  `description` text,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `slug` (`slug`),
  CONSTRAINT `ck_category_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'INACTIVE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dispute_evidences` (
  `evidence_id` bigint NOT NULL AUTO_INCREMENT,
  `dispute_id` bigint NOT NULL,
  `uploaded_by` bigint NOT NULL,
  `file_url` varchar(500) NOT NULL,
  `file_type` varchar(30) NOT NULL,
  `description` text,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`evidence_id`),
  KEY `dispute_id` (`dispute_id`),
  KEY `uploaded_by` (`uploaded_by`),
  CONSTRAINT `dispute_evidences_ibfk_1` FOREIGN KEY (`dispute_id`) REFERENCES `disputes` (`dispute_id`),
  CONSTRAINT `dispute_evidences_ibfk_2` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disputes` (
  `dispute_id` bigint NOT NULL AUTO_INCREMENT,
  `market_order_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `reason` varchar(50) NOT NULL,
  `description` text NOT NULL,
  `status` varchar(30) NOT NULL,
  `resolution` varchar(50) DEFAULT NULL,
  `resolution_note` text,
  `resolved_by` bigint DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`dispute_id`),
  UNIQUE KEY `market_order_id` (`market_order_id`),
  KEY `created_by` (`created_by`),
  KEY `resolved_by` (`resolved_by`),
  CONSTRAINT `disputes_ibfk_1` FOREIGN KEY (`market_order_id`) REFERENCES `marketplace_orders` (`market_order_id`),
  CONSTRAINT `disputes_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `disputes_ibfk_3` FOREIGN KEY (`resolved_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `escrow_transactions` (
  `escrow_id` bigint NOT NULL AUTO_INCREMENT,
  `market_order_id` bigint NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `status` varchar(30) NOT NULL,
  `payment_reference` varchar(255) DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `held_at` datetime(6) DEFAULT NULL,
  `released_at` datetime(6) DEFAULT NULL,
  `refunded_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`escrow_id`),
  UNIQUE KEY `market_order_id` (`market_order_id`),
  CONSTRAINT `escrow_transactions_ibfk_1` FOREIGN KEY (`market_order_id`) REFERENCES `marketplace_orders` (`market_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorites` (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `user_id` (`user_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inspection_reports` (
  `report_id` bigint NOT NULL AUTO_INCREMENT,
  `inspection_id` bigint NOT NULL,
  `model_verified` tinyint(1) NOT NULL,
  `serial_verified` tinyint(1) NOT NULL,
  `frame_condition` varchar(50) NOT NULL,
  `paint_condition` varchar(50) NOT NULL,
  `grommet_condition` varchar(50) NOT NULL,
  `handle_condition` varchar(50) NOT NULL,
  `crack_detected` tinyint(1) NOT NULL,
  `repair_detected` tinyint(1) NOT NULL,
  `overall_score` decimal(4,2) NOT NULL,
  `recommended_min_price` decimal(15,2) NOT NULL,
  `recommended_max_price` decimal(15,2) NOT NULL,
  `notes` text,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `inspection_id` (`inspection_id`),
  CONSTRAINT `inspection_reports_ibfk_1` FOREIGN KEY (`inspection_id`) REFERENCES `inspection_requests` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inspection_requests` (
  `inspection_id` bigint NOT NULL AUTO_INCREMENT,
  `listing_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `inspector_id` bigint DEFAULT NULL,
  `inspection_code` varchar(50) NOT NULL,
  `status` varchar(30) NOT NULL,
  `requested_at` datetime(6) NOT NULL,
  `accepted_at` datetime(6) DEFAULT NULL,
  `completed_at` datetime(6) DEFAULT NULL,
  `note` text,
  PRIMARY KEY (`inspection_id`),
  UNIQUE KEY `inspection_code` (`inspection_code`),
  KEY `listing_id` (`listing_id`),
  KEY `seller_id` (`seller_id`),
  KEY `inspector_id` (`inspector_id`),
  CONSTRAINT `inspection_requests_ibfk_1` FOREIGN KEY (`listing_id`) REFERENCES `used_listings` (`listing_id`),
  CONSTRAINT `inspection_requests_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `inspection_requests_ibfk_3` FOREIGN KEY (`inspector_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `marketplace_orders` (
  `market_order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_code` varchar(50) NOT NULL,
  `listing_id` bigint NOT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `item_price` decimal(15,2) NOT NULL,
  `shipping_fee` decimal(15,2) NOT NULL,
  `platform_fee` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `order_status` varchar(30) NOT NULL,
  `receiver_name` varchar(100) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `shipping_address` varchar(500) NOT NULL,
  `cancelled_by_type` varchar(30) DEFAULT NULL,
  `cancellation_reason` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`market_order_id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `listing_id` (`listing_id`),
  KEY `buyer_id` (`buyer_id`),
  KEY `seller_id` (`seller_id`),
  KEY `idx_market_status` (`order_status`),
  CONSTRAINT `marketplace_orders_ibfk_1` FOREIGN KEY (`listing_id`) REFERENCES `used_listings` (`listing_id`),
  CONSTRAINT `marketplace_orders_ibfk_2` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `marketplace_orders_ibfk_3` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text,
  `reference_type` varchar(50) NOT NULL,
  `reference_id` bigint NOT NULL,
  `is_read` tinyint(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`notification_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `product_image` varchar(500) DEFAULT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `total_price` decimal(15,2) NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  `variant_sku` varchar(100) DEFAULT NULL,
  `variant_color` varchar(80) DEFAULT NULL,
  `variant_size` varchar(50) DEFAULT NULL,
  `legacy_stock_rebased` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`order_item_id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  KEY `fk_order_items_variant` (`variant_id`),
  CONSTRAINT `fk_order_items_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `order_items_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `order_code` varchar(50) NOT NULL,
  `receiver_name` varchar(100) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `shipping_address` varchar(500) NOT NULL,
  `subtotal_amount` decimal(15,2) NOT NULL,
  `shipping_fee` decimal(15,2) NOT NULL,
  `discount_amount` decimal(15,2) NOT NULL,
  `total_amount` decimal(15,2) NOT NULL,
  `payment_method` varchar(30) NOT NULL,
  `payment_status` varchar(30) NOT NULL,
  `order_status` varchar(30) NOT NULL,
  `note` text,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `player_profiles` (
  `profile_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `skill_level` varchar(30) NOT NULL,
  `play_style` varchar(30) NOT NULL,
  `play_type` varchar(30) NOT NULL,
  `wrist_strength` varchar(30) NOT NULL,
  `preferred_weight` varchar(10) NOT NULL,
  `min_budget` decimal(15,2) NOT NULL,
  `max_budget` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `dominant_hand` varchar(10) DEFAULT NULL,
  `balance_type` varchar(20) DEFAULT NULL,
  `shaft_flexibility` varchar(20) DEFAULT NULL,
  `shirt_size` varchar(12) DEFAULT NULL,
  `pants_size` varchar(12) DEFAULT NULL,
  `shoe_size` varchar(12) DEFAULT NULL,
  `favorite_colors` varchar(255) DEFAULT NULL,
  `favorite_brands` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`profile_id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `player_profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `sort_order` int NOT NULL,
  `is_primary` tinyint(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_images_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `variant_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `sku` varchar(100) NOT NULL,
  `color_name` varchar(80) NOT NULL,
  `size_label` varchar(50) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `status` varchar(30) NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`variant_id`),
  UNIQUE KEY `uq_product_variants_sku` (`sku`),
  UNIQUE KEY `uq_product_variants_option` (`product_id`,`color_name`,`size_label`),
  CONSTRAINT `fk_product_variants_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `chk_product_variants_stock` CHECK ((`stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL,
  `brand_id` bigint NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `description` text,
  `price` decimal(15,2) NOT NULL,
  `sale_price` decimal(15,2) DEFAULT NULL,
  `stock` int NOT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `promotion_published` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `category_id` (`category_id`),
  KEY `brand_id` (`brand_id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_product_price` (`price`),
  KEY `idx_products_promotion_status` (`promotion_published`,`status`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`),
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`brand_id`),
  CONSTRAINT `products_chk_1` CHECK ((`stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `racket_specifications` (
  `specification_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `weight_class` varchar(10) NOT NULL,
  `balance_type` varchar(30) NOT NULL,
  `balance_point_mm` int DEFAULT NULL,
  `shaft_flexibility` varchar(30) NOT NULL,
  `max_tension_lbs` decimal(5,2) DEFAULT NULL,
  `play_style` varchar(30) NOT NULL,
  `skill_level` varchar(30) NOT NULL,
  `grip_size` varchar(20) DEFAULT NULL,
  `racket_length_mm` int DEFAULT NULL,
  `material` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`specification_id`),
  UNIQUE KEY `product_id` (`product_id`),
  CONSTRAINT `racket_specifications_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_logs` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `match_score` decimal(5,2) NOT NULL,
  `reason_json` json NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`log_id`),
  KEY `user_id` (`user_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `recommendation_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `recommendation_logs_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_weights` (
  `weight_id` bigint NOT NULL AUTO_INCREMENT,
  `skill_weight` decimal(5,2) NOT NULL,
  `play_style_weight` decimal(5,2) NOT NULL,
  `wrist_weight` decimal(5,2) NOT NULL,
  `weight_class_weight` decimal(5,2) NOT NULL,
  `flex_weight` decimal(5,2) NOT NULL,
  `budget_weight` decimal(5,2) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`weight_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `order_item_id` bigint NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` text,
  `status` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `user_id` (`user_id`,`order_item_id`),
  KEY `product_id` (`product_id`),
  KEY `order_item_id` (`order_item_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`),
  CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`order_item_id`),
  CONSTRAINT `reviews_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_ratings` (
  `rating_id` bigint NOT NULL AUTO_INCREMENT,
  `market_order_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `buyer_id` bigint NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` text,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `market_order_id` (`market_order_id`),
  KEY `seller_id` (`seller_id`),
  KEY `buyer_id` (`buyer_id`),
  CONSTRAINT `seller_ratings_ibfk_1` FOREIGN KEY (`market_order_id`) REFERENCES `marketplace_orders` (`market_order_id`),
  CONSTRAINT `seller_ratings_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `seller_ratings_ibfk_3` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `seller_ratings_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipments` (
  `shipment_id` bigint NOT NULL AUTO_INCREMENT,
  `market_order_id` bigint NOT NULL,
  `carrier` varchar(100) NOT NULL,
  `tracking_code` varchar(100) NOT NULL,
  `shipping_status` varchar(30) NOT NULL,
  `shipped_at` datetime(6) DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`shipment_id`),
  UNIQUE KEY `market_order_id` (`market_order_id`),
  CONSTRAINT `shipments_ibfk_1` FOREIGN KEY (`market_order_id`) REFERENCES `marketplace_orders` (`market_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_payments` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `reference` varchar(50) NOT NULL,
  `method` varchar(30) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'UNPAID',
  `checkout_url` text,
  `bank_name` varchar(100) DEFAULT NULL,
  `bank_id` varchar(30) DEFAULT NULL,
  `account_number` varchar(30) DEFAULT NULL,
  `account_name` varchar(100) DEFAULT NULL,
  `external_key` varchar(200) DEFAULT NULL,
  `refund_key` varchar(200) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `uk_shop_payment_order` (`order_id`),
  UNIQUE KEY `uk_shop_payment_reference` (`reference`),
  UNIQUE KEY `uk_shop_payment_external` (`external_key`),
  UNIQUE KEY `uk_shop_payment_refund` (`refund_key`),
  CONSTRAINT `fk_shop_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `ck_shop_payment_amount` CHECK ((`amount` > 0)),
  CONSTRAINT `ck_shop_payment_method` CHECK ((`method` in (_utf8mb4'BANK_TRANSFER',_utf8mb4'VIETQR',_utf8mb4'VNPAY',_utf8mb4'ZALOPAY',_utf8mb4'MOMO'))),
  CONSTRAINT `ck_shop_payment_status` CHECK ((`status` in (_utf8mb4'UNPAID',_utf8mb4'PAID',_utf8mb4'FAILED',_utf8mb4'REFUND_PENDING',_utf8mb4'REFUNDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_posts` (
  `post_id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(180) NOT NULL,
  `summary` varchar(400) NOT NULL,
  `content` mediumtext NOT NULL,
  `category` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'DRAFT',
  `cover_image_url` varchar(500) DEFAULT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`post_id`),
  KEY `idx_shop_posts_publication` (`status`,`published_at`,`post_id`),
  CONSTRAINT `chk_shop_posts_category` CHECK ((`category` in (_utf8mb4'NEWS',_utf8mb4'COMMUNITY'))),
  CONSTRAINT `chk_shop_posts_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'PUBLISHED',_utf8mb4'ARCHIVED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `used_listing_images` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `listing_id` bigint NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `image_type` varchar(30) NOT NULL,
  `sort_order` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `listing_id` (`listing_id`),
  CONSTRAINT `used_listing_images_ibfk_1` FOREIGN KEY (`listing_id`) REFERENCES `used_listings` (`listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `used_listings` (
  `listing_id` bigint NOT NULL AUTO_INCREMENT,
  `seller_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  `brand_id` bigint NOT NULL,
  `listing_code` varchar(50) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `model` varchar(150) NOT NULL,
  `description` text,
  `original_price` decimal(15,2) NOT NULL,
  `selling_price` decimal(15,2) NOT NULL,
  `purchase_date` date DEFAULT NULL,
  `usage_months` int NOT NULL,
  `condition_grade` varchar(10) NOT NULL,
  `condition_score` decimal(4,2) DEFAULT NULL,
  `warranty_status` varchar(30) NOT NULL,
  `warranty_until` date DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `view_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`listing_id`),
  UNIQUE KEY `listing_code` (`listing_code`),
  KEY `seller_id` (`seller_id`),
  KEY `category_id` (`category_id`),
  KEY `brand_id` (`brand_id`),
  KEY `idx_listing_status` (`status`),
  KEY `idx_listing_price` (`selling_price`),
  CONSTRAINT `used_listings_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `used_listings_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`),
  CONSTRAINT `used_listings_ibfk_3` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `used_racket_specifications` (
  `used_spec_id` bigint NOT NULL AUTO_INCREMENT,
  `listing_id` bigint NOT NULL,
  `weight_class` varchar(10) NOT NULL,
  `grip_size` varchar(20) DEFAULT NULL,
  `string_model` varchar(100) DEFAULT NULL,
  `string_tension_lbs` decimal(5,2) DEFAULT NULL,
  `frame_condition` varchar(50) NOT NULL,
  `paint_condition` varchar(50) NOT NULL,
  `grommet_condition` varchar(50) NOT NULL,
  `handle_condition` varchar(50) NOT NULL,
  `repair_history` text,
  `serial_number` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`used_spec_id`),
  UNIQUE KEY `listing_id` (`listing_id`),
  CONSTRAINT `used_racket_specifications_ibfk_1` FOREIGN KEY (`listing_id`) REFERENCES `used_listings` (`listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `role` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` varchar(12) DEFAULT NULL,
  `height_cm` int DEFAULT NULL,
  `weight_kg` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_phone` (`phone`),
  CONSTRAINT `ck_users_role` CHECK ((`role` in (_utf8mb4'USER',_utf8mb4'INSPECTOR',_utf8mb4'ADMIN'))),
  CONSTRAINT `ck_users_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'LOCKED',_utf8mb4'SUSPENDED',_utf8mb4'DELETED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `valuations` (
  `valuation_id` bigint NOT NULL AUTO_INCREMENT,
  `listing_id` bigint NOT NULL,
  `reference_price` decimal(15,2) NOT NULL,
  `age_factor` decimal(5,3) NOT NULL,
  `condition_factor` decimal(5,3) NOT NULL,
  `warranty_factor` decimal(5,3) NOT NULL,
  `accessory_factor` decimal(5,3) NOT NULL,
  `repair_factor` decimal(5,3) NOT NULL,
  `demand_factor` decimal(5,3) NOT NULL,
  `estimated_price` decimal(15,2) NOT NULL,
  `suggested_min_price` decimal(15,2) NOT NULL,
  `suggested_max_price` decimal(15,2) NOT NULL,
  `valuation_method` varchar(30) NOT NULL,
  `condition_score` decimal(4,2) DEFAULT NULL,
  `algorithm_version` varchar(50) NOT NULL,
  `note` text,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`valuation_id`),
  KEY `listing_id` (`listing_id`),
  CONSTRAINT `valuations_ibfk_1` FOREIGN KEY (`listing_id`) REFERENCES `used_listings` (`listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (1,'1','create users and addresses','SQL','V1__create_users_and_addresses.sql',-1132614362,'root','2026-10-12 22:20:28',69,1),(2,'2','shop recommendation marketplace','SQL','V2__shop_recommendation_marketplace.sql',347797368,'root','2026-10-12 22:20:29',881,1),(3,'3','approved weights and indexes','SQL','V3__approved_weights_and_indexes.sql',1754042599,'root','2026-10-12 22:20:30',206,1),(4,'4','catalog status policy','SQL','V4__catalog_status_policy.sql',-384319459,'root','2026-10-12 22:20:30',124,1),(5,'5','contact verification','SQL','V5__contact_verification.sql',1927246060,'root','2026-10-12 22:20:30',192,1),(6,'6','remove contact verification','SQL','V6__remove_contact_verification.sql',1956644679,'root','2026-10-12 22:20:30',82,1),(7,'7','product variants','SQL','V7__product_variants.sql',1939081610,'root','2026-10-12 22:20:30',429,1),(8,'8','variant seed column names','SQL','V8__variant_seed_column_names.sql',1008792720,'root','2026-10-12 22:20:31',49,1),(9,'9','extend profile preferences','SQL','V9__extend_profile_preferences.sql',-1587339318,'root','2026-10-12 22:20:31',144,1),(10,'10','admin published promotions','SQL','V10__admin_published_promotions.sql',-309675956,'root','2026-10-12 22:20:31',113,1),(11,'11','shop posts','SQL','V11__shop_posts.sql',1103470173,'root','2026-10-12 22:20:31',29,1),(12,'12','catalog import sources','SQL','V12__catalog_import_sources.sql',1231281990,'root','2026-10-12 22:20:31',39,1),(13,'13','shop payments','SQL','V13__shop_payments.sql',-1658864352,'root','2026-10-12 22:20:31',38,1);

INSERT INTO `recommendation_weights` (`weight_id`, `skill_weight`, `play_style_weight`, `wrist_weight`, `weight_class_weight`, `flex_weight`, `budget_weight`, `is_active`, `created_at`, `updated_at`) VALUES (1,20.00,25.00,15.00,15.00,10.00,15.00,1,'2026-10-12 22:20:29.859617','2026-10-12 22:20:29.859617');
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


END$$
CALL dh_install_schema_v13()$$
DROP PROCEDURE dh_install_schema_v13$$
DELIMITER ;

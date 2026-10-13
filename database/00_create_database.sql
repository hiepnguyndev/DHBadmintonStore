-- MySQL 8.0+: create/select the application database, without changing accounts.
-- Existing data is preserved. Database credentials belong in backend/.env.
CREATE DATABASE IF NOT EXISTS badminton_ecommerce
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE badminton_ecommerce;
SET NAMES utf8mb4;

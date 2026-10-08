-- Run once using an administrator connection in MySQL Workbench.
-- Does not delete databases, create privileged users, or insert demo accounts.
CREATE DATABASE IF NOT EXISTS badminton_ecommerce
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- Create a dedicated badminton_app account with your own password in Workbench.
-- Grant privileges on badminton_ecommerce.* only; Flyway needs DDL rights locally.
CREATE USER IF NOT EXISTS 'badminton_app'@'localhost'
  IDENTIFIED BY '220305';

ALTER USER 'badminton_app'@'localhost'
  IDENTIFIED BY '220305';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES, DROP
ON badminton_ecommerce.* TO 'badminton_app'@'localhost';
SHOW GRANTS FOR 'badminton_app'@'localhost';
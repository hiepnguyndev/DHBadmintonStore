ALTER TABLE users
  ADD COLUMN date_of_birth DATE NULL,
  ADD COLUMN gender VARCHAR(12) NULL,
  ADD COLUMN height_cm INT NULL,
  ADD COLUMN weight_kg DECIMAL(5,2) NULL;

ALTER TABLE player_profiles
  ADD COLUMN dominant_hand VARCHAR(10) NULL,
  ADD COLUMN balance_type VARCHAR(20) NULL,
  ADD COLUMN shaft_flexibility VARCHAR(20) NULL,
  ADD COLUMN shirt_size VARCHAR(12) NULL,
  ADD COLUMN pants_size VARCHAR(12) NULL,
  ADD COLUMN shoe_size VARCHAR(12) NULL,
  ADD COLUMN favorite_colors VARCHAR(255) NULL,
  ADD COLUMN favorite_brands VARCHAR(255) NULL;

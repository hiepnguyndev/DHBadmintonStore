UPDATE users
SET phone = NULL
WHERE phone IS NOT NULL AND TRIM(phone) = '';

ALTER TABLE users
    MODIFY COLUMN email VARCHAR(150) NULL;

UPDATE users
SET email_verified = TRUE
WHERE email IS NOT NULL;

ALTER TABLE users
    ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT uk_users_phone UNIQUE (phone);

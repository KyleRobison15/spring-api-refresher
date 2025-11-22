-- Migration to transform users table to match BaseUser from krd-spring-starters
-- Changes:
-- 1. Split `name` into `first_name` and `last_name`
-- 2. Move single `role` column to `user_roles` table (Set<Role>)
-- 3. Transform `addresses` table to `user_addresses` embedded collection
-- 4. Add timestamp columns (`created_at`, `updated_at`)

-- Step 1: Add new columns to users table
ALTER TABLE users
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Step 2: Migrate data from `name` to `first_name` and `last_name`
-- Split name by first space (everything before first space = first_name, rest = last_name)
UPDATE users
SET first_name = SUBSTRING_INDEX(name, ' ', 1),
    last_name = CASE
                    WHEN LOCATE(' ', name) > 0
                        THEN SUBSTRING(name, LOCATE(' ', name) + 1)
                    ELSE ''
        END;

-- Step 3: Drop the old `name` column
ALTER TABLE users
    DROP COLUMN name;

-- Step 4: Create user_roles table for Set<Role>
CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);

-- Step 5: Migrate existing role data to user_roles table
INSERT INTO user_roles (user_id, role)
SELECT id, CONCAT('ROLE_', UPPER(role))
FROM users
WHERE role IS NOT NULL;

-- Step 6: Drop the old role column from users table
ALTER TABLE users
    DROP COLUMN role;

-- Step 7: Create user_addresses table (embedded collection)
CREATE TABLE user_addresses
(
    user_id    BIGINT       NOT NULL,
    street     VARCHAR(255),
    city       VARCHAR(255),
    zip_code   VARCHAR(255),
    state      VARCHAR(255),
    country    VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_addresses_user_id ON user_addresses (user_id);

-- Step 8: Migrate data from addresses table to user_addresses
INSERT INTO user_addresses (user_id, street, city, zip_code, state, country, is_default)
SELECT user_id, street, city, zip, state, 'USA', FALSE
FROM addresses;

-- Step 9: Drop old addresses table and its constraints
DROP TABLE addresses;

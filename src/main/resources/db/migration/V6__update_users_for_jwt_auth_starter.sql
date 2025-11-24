-- Add new columns to users table
ALTER TABLE users
    ADD COLUMN first_name VARCHAR(255),
    ADD COLUMN last_name VARCHAR(255),
    ADD COLUMN username VARCHAR(255) UNIQUE,
    ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;

-- Migrate existing name data to first_name
-- Note: This copies the full name to first_name. Manual data cleanup may be needed.
UPDATE users SET first_name = name WHERE first_name IS NULL;

-- Generate username from email (everything before @)
UPDATE users SET username = SUBSTRING_INDEX(email, '@', 1) WHERE username IS NULL;

-- Make first_name NOT NULL after migration
ALTER TABLE users
    MODIFY COLUMN first_name VARCHAR(255) NOT NULL;

-- Create user_roles table for many-to-many relationship
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role    VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role)
);

-- Migrate existing role data to user_roles table
INSERT INTO user_roles (user_id, role)
SELECT id, role FROM users WHERE role IS NOT NULL;

-- Drop the old role column
ALTER TABLE users DROP COLUMN role;

-- Drop the old name column
ALTER TABLE users DROP COLUMN name;

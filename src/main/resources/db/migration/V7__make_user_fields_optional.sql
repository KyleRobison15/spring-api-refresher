-- Make firstName, lastName, and username optional (nullable)
-- Only email and password are required fields

ALTER TABLE users
    MODIFY COLUMN first_name VARCHAR(255) NULL,
    MODIFY COLUMN last_name VARCHAR(255) NULL,
    MODIFY COLUMN username VARCHAR(255) NULL;

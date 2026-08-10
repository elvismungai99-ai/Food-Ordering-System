ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role VARCHAR(255),
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE users
SET
    first_name = COALESCE(first_name, 'Unknown'),
    last_name = COALESCE(last_name, 'User'),
    full_name = COALESCE(full_name, NULLIF(CONCAT_WS(' ', first_name, last_name), ''), email, 'Unknown User'),
    password_hash = COALESCE(password_hash, '$2a$10$dIRwAuAFUTFpvD31HJfrhe8K5iB3t8iCbCTZ1xLDWyESTLt0bncHu'),
    role = COALESCE(role, 'CUSTOMER'),
    is_active = COALESCE(is_active, true),
    updated_at = COALESCE(updated_at, created_at, now());

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ALTER COLUMN password_hash SET NOT NULL,
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN is_active SET NOT NULL;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role VARCHAR(50);

UPDATE users
SET
    first_name = COALESCE(first_name, username),
    last_name = COALESCE(last_name, 'User'),
    role = COALESCE(role, 'CUSTOMER');

ALTER TABLE users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ALTER COLUMN role SET NOT NULL;

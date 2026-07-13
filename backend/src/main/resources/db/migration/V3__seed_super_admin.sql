ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('CUSTOMER', 'OWNER', 'SUPER_ADMIN'));

INSERT INTO users (id, email, password_hash, first_name, last_name, full_name, role, is_active, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'admin@foodordering.com',
    '$2a$10$dIRwAuAFUTFpvD31HJfrhe8K5iB3t8iCbCTZ1xLDWyESTLt0bncHu',
    'System',
    'Admin',
    'System Admin',
    'SUPER_ADMIN',
    true,
    now(),
    now()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@foodordering.com'
);
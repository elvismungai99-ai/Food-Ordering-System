-- V9: Add phone number to users and create saved_addresses table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50);

CREATE TABLE IF NOT EXISTS saved_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label VARCHAR(50) NOT NULL DEFAULT 'Home',
    address TEXT NOT NULL,
    building_name VARCHAR(150),
    apartment_number VARCHAR(100),
    landmarks TEXT,
    delivery_instructions TEXT,
    latitude NUMERIC(10, 6),
    longitude NUMERIC(10, 6),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saved_addresses_user_id ON saved_addresses(user_id);


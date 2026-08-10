-- Align the original placeholder schema with the current application entities.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'users'
          AND column_name = 'id'
          AND data_type <> 'uuid'
    ) THEN
        ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_user_id_fkey;
        ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
        ALTER TABLE users ADD COLUMN IF NOT EXISTS legacy_id INTEGER;
        UPDATE users SET legacy_id = id WHERE legacy_id IS NULL;
        ALTER TABLE users ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE users ALTER COLUMN id TYPE UUID USING gen_random_uuid();
        ALTER TABLE users ALTER COLUMN id SET DEFAULT gen_random_uuid();
        ALTER TABLE users ADD PRIMARY KEY (id);
    END IF;
END $$;

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

CREATE TABLE IF NOT EXISTS restaurants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT,
    opening_time TIME,
    closing_time TIME,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING_APPROVAL',
    category VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS menu_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    category VARCHAR(255),
    available BOOLEAN NOT NULL DEFAULT true,
    image_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS menu_item_add_ons (
    menu_item_id UUID NOT NULL REFERENCES menu_items(id),
    add_on VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES carts(id),
    menu_item_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_items_cart_menu_item UNIQUE (cart_id, menu_item_id)
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'orders'
          AND column_name = 'id'
          AND data_type <> 'uuid'
    ) THEN
        ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_pkey;
        ALTER TABLE orders ADD COLUMN IF NOT EXISTS legacy_id INTEGER;
        UPDATE orders SET legacy_id = id WHERE legacy_id IS NULL;
        ALTER TABLE orders ALTER COLUMN id DROP DEFAULT;
        ALTER TABLE orders ALTER COLUMN id TYPE UUID USING gen_random_uuid();
        ALTER TABLE orders ALTER COLUMN id SET DEFAULT gen_random_uuid();
        ALTER TABLE orders ADD PRIMARY KEY (id);
    END IF;
END $$;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS customer_id UUID,
    ADD COLUMN IF NOT EXISTS restaurant_id UUID,
    ADD COLUMN IF NOT EXISTS restaurant_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS delivery_address TEXT,
    ADD COLUMN IF NOT EXISTS delivery_latitude NUMERIC(9, 6),
    ADD COLUMN IF NOT EXISTS delivery_longitude NUMERIC(10, 6),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(255) DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(255) DEFAULT 'CASH_ON_DELIVERY',
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT,
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS subtotal_amount NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS service_fee NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12, 2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE orders
SET
    customer_id = COALESCE(customer_id, (SELECT id FROM users ORDER BY created_at NULLS LAST LIMIT 1), gen_random_uuid()),
    restaurant_id = COALESCE(restaurant_id, gen_random_uuid()),
    restaurant_name = COALESCE(restaurant_name, 'Unknown Restaurant'),
    delivery_address = COALESCE(delivery_address, 'Unknown Address'),
    payment_status = COALESCE(payment_status, 'PENDING'),
    payment_method = COALESCE(payment_method, 'CASH_ON_DELIVERY'),
    total_amount = COALESCE(total_amount, 0),
    subtotal_amount = COALESCE(subtotal_amount, 0),
    delivery_fee = COALESCE(delivery_fee, 0),
    service_fee = COALESCE(service_fee, 0),
    tax_amount = COALESCE(tax_amount, 0),
    discount_amount = COALESCE(discount_amount, 0),
    updated_at = COALESCE(updated_at, created_at, now());

ALTER TABLE orders
    ALTER COLUMN customer_id SET NOT NULL,
    ALTER COLUMN restaurant_id SET NOT NULL,
    ALTER COLUMN restaurant_name SET NOT NULL,
    ALTER COLUMN delivery_address SET NOT NULL,
    ALTER COLUMN payment_status SET NOT NULL,
    ALTER COLUMN payment_method SET NOT NULL,
    ALTER COLUMN total_amount SET NOT NULL,
    ALTER COLUMN subtotal_amount SET NOT NULL,
    ALTER COLUMN delivery_fee SET NOT NULL,
    ALTER COLUMN service_fee SET NOT NULL,
    ALTER COLUMN tax_amount SET NOT NULL,
    ALTER COLUMN discount_amount SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    menu_item_id UUID,
    item_name VARCHAR(255) NOT NULL,
    item_description TEXT,
    image_url TEXT,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    menu_item_id UUID,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS riders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    vehicle_type VARCHAR(255) NOT NULL,
    licence_plate VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING_APPROVAL',
    operational_status VARCHAR(255) NOT NULL DEFAULT 'CLOSED',
    online BOOLEAN NOT NULL DEFAULT false,
    total_rejections INTEGER NOT NULL DEFAULT 0,
    current_latitude NUMERIC(9, 6),
    current_longitude NUMERIC(10, 6),
    last_location_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delivery_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    rider_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    restaurant_name VARCHAR(255) NOT NULL,
    restaurant_address TEXT NOT NULL,
    restaurant_latitude NUMERIC(9, 6),
    restaurant_longitude NUMERIC(10, 6),
    customer_address TEXT NOT NULL,
    customer_latitude NUMERIC(9, 6),
    customer_longitude NUMERIC(10, 6),
    distance_km NUMERIC(8, 2),
    estimated_payout NUMERIC(12, 2) NOT NULL,
    assignment_score NUMERIC(10, 2),
    status VARCHAR(255) NOT NULL DEFAULT 'REQUESTED',
    rejection_reason TEXT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    arrived_at_restaurant_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

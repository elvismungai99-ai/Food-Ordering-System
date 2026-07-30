ALTER TABLE restaurants
    ADD COLUMN IF NOT EXISTS owner_id UUID,
    ADD COLUMN IF NOT EXISTS name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS address TEXT,
    ADD COLUMN IF NOT EXISTS opening_time TIME,
    ADD COLUMN IF NOT EXISTS closing_time TIME,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50),
    ADD COLUMN IF NOT EXISTS category VARCHAR(100),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE restaurants
    DROP CONSTRAINT IF EXISTS restaurants_status_check;

UPDATE restaurants
SET status = 'APPROVED'
WHERE status IS NULL
   OR status IN ('OPEN', 'CLOSED');

ALTER TABLE restaurants
    ADD CONSTRAINT restaurants_status_check
        CHECK (
            status IN (
                'PENDING_APPROVAL',
                'APPROVED',
                'SUSPENDED',
                'REJECTED'
            )
        );

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT,
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS menu_item_add_ons (
    menu_item_id UUID NOT NULL,
    add_on VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    menu_item_id UUID,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reviews_scope
    ON reviews (
        order_id,
        customer_id,
        restaurant_id,
        COALESCE(menu_item_id, '00000000-0000-0000-0000-000000000000'::uuid)
    );

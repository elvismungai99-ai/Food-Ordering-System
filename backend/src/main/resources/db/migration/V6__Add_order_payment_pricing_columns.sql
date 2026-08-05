ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH_ON_DELIVERY',
    ADD COLUMN IF NOT EXISTS subtotal_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_fee NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS service_fee NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_payment_method_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_payment_method_check
        CHECK (payment_method IN ('MPESA', 'CASH_ON_DELIVERY'));

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_payment_status_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_payment_status_check
        CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED'));

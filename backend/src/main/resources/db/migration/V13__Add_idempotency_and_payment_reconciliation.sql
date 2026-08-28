-- Migration V13: Add Idempotency, Unique Provider Transaction IDs, and Refund Reconciliation to Orders

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(128),
    ADD COLUMN IF NOT EXISTS provider_transaction_id VARCHAR(128),
    ADD COLUMN IF NOT EXISTS refund_reason TEXT,
    ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS refund_reference VARCHAR(128);

-- Unique index to prevent duplicate checkout submissions per customer
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_customer_idempotency_key
    ON orders (customer_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- Unique index to ensure payment provider transaction receipts (e.g. M-Pesa receipt) are strictly unique
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_provider_transaction_id
    ON orders (provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;


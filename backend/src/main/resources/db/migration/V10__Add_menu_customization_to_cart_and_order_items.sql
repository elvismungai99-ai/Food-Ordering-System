-- V10: Support item customization (size, add-ons, removals, instructions)
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS selected_size VARCHAR(50),
    ADD COLUMN IF NOT EXISTS selected_add_ons TEXT,
    ADD COLUMN IF NOT EXISTS special_instructions TEXT,
    ADD COLUMN IF NOT EXISTS removal_requests TEXT;

-- Drop unique constraint so customers can have different customizations of the same menu item
ALTER TABLE cart_items
    DROP CONSTRAINT IF EXISTS uk_cart_items_cart_menu_item;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS selected_size VARCHAR(50),
    ADD COLUMN IF NOT EXISTS selected_add_ons TEXT,
    ADD COLUMN IF NOT EXISTS special_instructions TEXT,
    ADD COLUMN IF NOT EXISTS removal_requests TEXT;


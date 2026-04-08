-- ============================================================
-- FASE 4: Migrate sellPrice columns from VARCHAR to NUMERIC
-- ============================================================

-- 1. products.sell_price: VARCHAR(50) → NUMERIC(12,2)
ALTER TABLE products ADD COLUMN sell_price_new NUMERIC(12,2);

UPDATE products SET sell_price_new = CAST(sell_price AS NUMERIC(12,2))
WHERE sell_price IS NOT NULL AND sell_price ~ '^\d+\.?\d*$';

ALTER TABLE products DROP COLUMN sell_price;
ALTER TABLE products RENAME COLUMN sell_price_new TO sell_price;

-- 2. product_details.sell_price: VARCHAR(50) → NUMERIC(12,2)
ALTER TABLE product_details ADD COLUMN sell_price_new NUMERIC(12,2);

UPDATE product_details SET sell_price_new = CAST(sell_price AS NUMERIC(12,2))
WHERE sell_price IS NOT NULL AND sell_price ~ '^\d+\.?\d*$';

ALTER TABLE product_details DROP COLUMN sell_price;
ALTER TABLE product_details RENAME COLUMN sell_price_new TO sell_price;

-- 3. product_details.suggest_sell_price: VARCHAR(50) → NUMERIC(12,2)
ALTER TABLE product_details ADD COLUMN suggest_sell_price_new NUMERIC(12,2);

UPDATE product_details SET suggest_sell_price_new = CAST(suggest_sell_price AS NUMERIC(12,2))
WHERE suggest_sell_price IS NOT NULL AND suggest_sell_price ~ '^\d+\.?\d*$';

ALTER TABLE product_details DROP COLUMN suggest_sell_price;
ALTER TABLE product_details RENAME COLUMN suggest_sell_price_new TO suggest_sell_price;

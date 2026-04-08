-- ============================================================
-- Add price range columns to price_rules
-- Enables range-based margin rules (e.g. $0-$10 → 80%)
-- ============================================================

-- Add range columns
ALTER TABLE price_rules ADD COLUMN min_price NUMERIC(10,2) DEFAULT NULL;
ALTER TABLE price_rules ADD COLUMN max_price NUMERIC(10,2) DEFAULT NULL;

-- Drop old unique constraint (now multiple rules per scope with different ranges)
ALTER TABLE price_rules DROP CONSTRAINT IF EXISTS uq_price_rules_scope;

-- New unique index handling NULLs via COALESCE
CREATE UNIQUE INDEX uq_price_rules_scope_range
ON price_rules (scope, COALESCE(scope_id, ''), COALESCE(min_price, -1), COALESCE(max_price, -1));

-- Index for efficient range lookups
CREATE INDEX idx_price_rules_price_range ON price_rules (min_price, max_price);

-- Constraint: min_price < max_price when both are defined
ALTER TABLE price_rules ADD CONSTRAINT chk_price_range
  CHECK (min_price IS NULL OR max_price IS NULL OR min_price < max_price);

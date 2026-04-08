-- ============================================================
-- Price rules table for profit margin configuration
-- ============================================================

CREATE TABLE price_rules (
    id            VARCHAR(64)   PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
    scope         VARCHAR(20)    NOT NULL,
    scope_id      VARCHAR(100),
    margin_type   VARCHAR(20)    NOT NULL DEFAULT 'PERCENTAGE',
    margin_value  NUMERIC(10,2)  NOT NULL,
    priority      INTEGER        NOT NULL DEFAULT 0,
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_price_rules_scope       CHECK (scope IN ('GLOBAL','CATEGORY','PRODUCT','VARIANT')),
    CONSTRAINT chk_price_rules_margin_type CHECK (margin_type IN ('PERCENTAGE','FIXED')),
    CONSTRAINT chk_price_rules_margin_val  CHECK (margin_value >= 0),
    CONSTRAINT uq_price_rules_scope        UNIQUE (scope, scope_id)
);

CREATE INDEX idx_price_rules_scope    ON price_rules (scope);
CREATE INDEX idx_price_rules_active   ON price_rules (active);
CREATE INDEX idx_price_rules_scope_id ON price_rules (scope_id);

-- Default global rule: 40% profit margin
INSERT INTO price_rules (scope, scope_id, margin_type, margin_value, priority, active)
VALUES ('GLOBAL', NULL, 'PERCENTAGE', 40.00, 0, TRUE);

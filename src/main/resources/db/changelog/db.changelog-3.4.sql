-- ============================================================
-- Country tax rates table for IVA/VAT per country
-- ============================================================

CREATE TABLE country_taxes (
    id                  VARCHAR(64)    PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
    country_code        VARCHAR(3)     NOT NULL,
    region              VARCHAR(100),
    rate                NUMERIC(8,6)   NOT NULL,
    type                VARCHAR(20)    NOT NULL DEFAULT 'PERCENTAGE',
    applies_to          TEXT,
    includes_shipping   BOOLEAN        NOT NULL DEFAULT TRUE,
    active              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_country_taxes_type  CHECK (type IN ('PERCENTAGE','FIXED')),
    CONSTRAINT chk_country_taxes_rate  CHECK (rate >= 0)
);

CREATE INDEX idx_country_taxes_country ON country_taxes (country_code);
CREATE INDEX idx_country_taxes_active  ON country_taxes (active);
CREATE UNIQUE INDEX uq_country_taxes_scope
    ON country_taxes (country_code, COALESCE(region, ''), COALESCE(applies_to, ''));

-- ── Standard VAT / IVA rates per country ──────────────────────────────────────
INSERT INTO country_taxes (country_code, rate, type, applies_to, active) VALUES
    ('ES', 0.21,   'PERCENTAGE', 'General', TRUE),
    ('PT', 0.23,   'PERCENTAGE', 'General', TRUE),
    ('FR', 0.20,   'PERCENTAGE', 'General', TRUE),
    ('DE', 0.19,   'PERCENTAGE', 'General', TRUE),
    ('IT', 0.22,   'PERCENTAGE', 'General', TRUE),
    ('NL', 0.21,   'PERCENTAGE', 'General', TRUE),
    ('BE', 0.21,   'PERCENTAGE', 'General', TRUE),
    ('AT', 0.20,   'PERCENTAGE', 'General', TRUE),
    ('PL', 0.23,   'PERCENTAGE', 'General', TRUE),
    ('SE', 0.25,   'PERCENTAGE', 'General', TRUE),
    ('DK', 0.25,   'PERCENTAGE', 'General', TRUE),
    ('FI', 0.255,  'PERCENTAGE', 'General', TRUE),
    ('IE', 0.23,   'PERCENTAGE', 'General', TRUE),
    ('GR', 0.24,   'PERCENTAGE', 'General', TRUE),
    ('CZ', 0.21,   'PERCENTAGE', 'General', TRUE),
    ('RO', 0.19,   'PERCENTAGE', 'General', TRUE),
    ('HU', 0.27,   'PERCENTAGE', 'General', TRUE),
    ('GB', 0.20,   'PERCENTAGE', 'General', TRUE),
    ('CH', 0.081,  'PERCENTAGE', 'General', TRUE),
    ('NO', 0.25,   'PERCENTAGE', 'General', TRUE),
    ('US', 0.00,   'PERCENTAGE', 'General', TRUE),
    ('CA', 0.05,   'PERCENTAGE', 'General', TRUE),
    ('MX', 0.16,   'PERCENTAGE', 'General', TRUE),
    ('BR', 0.17,   'PERCENTAGE', 'General', TRUE),
    ('AR', 0.21,   'PERCENTAGE', 'General', TRUE),
    ('CO', 0.19,   'PERCENTAGE', 'General', TRUE),
    ('CL', 0.19,   'PERCENTAGE', 'General', TRUE),
    ('PE', 0.18,   'PERCENTAGE', 'General', TRUE),
    ('JP', 0.10,   'PERCENTAGE', 'General', TRUE),
    ('KR', 0.10,   'PERCENTAGE', 'General', TRUE),
    ('CN', 0.13,   'PERCENTAGE', 'General', TRUE),
    ('AU', 0.10,   'PERCENTAGE', 'General', TRUE),
    ('IN', 0.18,   'PERCENTAGE', 'General', TRUE);

-- ── Reduced rates for specific countries ──────────────────────────────────────
INSERT INTO country_taxes (country_code, rate, type, applies_to, active) VALUES
    ('ES', 0.10,   'PERCENTAGE', 'Alimentación', TRUE),
    ('ES', 0.04,   'PERCENTAGE', 'Medicamentos, Libros', TRUE),
    ('FR', 0.055,  'PERCENTAGE', 'Alimentación, Libros', TRUE),
    ('DE', 0.07,   'PERCENTAGE', 'Alimentación, Libros', TRUE),
    ('IT', 0.10,   'PERCENTAGE', 'Alimentación', TRUE),
    ('IT', 0.04,   'PERCENTAGE', 'Medicamentos, Libros', TRUE),
    ('PT', 0.06,   'PERCENTAGE', 'Alimentación, Medicamentos', TRUE),
    ('GB', 0.00,   'PERCENTAGE', 'Alimentación, Libros, Medicamentos', TRUE);

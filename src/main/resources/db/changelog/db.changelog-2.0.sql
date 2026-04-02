-- ============================================================
-- Brands: Tabla principal de marcas
-- ============================================================

CREATE TABLE IF NOT EXISTS brands (
    id              VARCHAR(64)     PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    logo_url        VARCHAR(500),
    website_url     VARCHAR(500),
    description     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE INDEX IF NOT EXISTS idx_brands_slug ON brands(slug);
CREATE INDEX IF NOT EXISTS idx_brands_status ON brands(status);
CREATE INDEX IF NOT EXISTS idx_brands_name ON brands(name);

-- ============================================================
-- Añadir brand_id a products
-- ============================================================

ALTER TABLE products ADD COLUMN IF NOT EXISTS brand_id VARCHAR(64) REFERENCES brands(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand_id);

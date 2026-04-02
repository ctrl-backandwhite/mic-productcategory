-- ============================================================
-- Attributes: Atributos de producto (Color, Talla, Material, etc.)
-- ============================================================

CREATE TABLE IF NOT EXISTS attributes (
    id              VARCHAR(64)     PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    slug            VARCHAR(255)    NOT NULL UNIQUE,
    type            VARCHAR(20)     NOT NULL DEFAULT 'TEXT',
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE INDEX IF NOT EXISTS idx_attributes_slug ON attributes(slug);

-- ============================================================
-- Attribute Values: Valores posibles de cada atributo
-- ============================================================

CREATE TABLE IF NOT EXISTS attribute_values (
    id              VARCHAR(64)     PRIMARY KEY,
    attribute_id    VARCHAR(64)     NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    value           VARCHAR(255)    NOT NULL,
    color_hex       VARCHAR(7),
    position        INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE INDEX IF NOT EXISTS idx_attribute_values_attribute ON attribute_values(attribute_id);
CREATE INDEX IF NOT EXISTS idx_attribute_values_position ON attribute_values(attribute_id, position);

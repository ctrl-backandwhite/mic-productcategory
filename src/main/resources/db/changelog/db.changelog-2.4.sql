-- liquibase formatted sql
-- changeset 14:warranties
-- comment: Tabla de garantías y FK en products

CREATE TABLE warranties (
    id                  VARCHAR(64)     PRIMARY KEY,
    name                VARCHAR(255)    NOT NULL,
    type                VARCHAR(32)     NOT NULL DEFAULT 'MANUFACTURER',
    duration_months     INT             NOT NULL DEFAULT 12,
    coverage            TEXT,
    conditions          TEXT,
    includes_labor      BOOLEAN         NOT NULL DEFAULT FALSE,
    includes_parts      BOOLEAN         NOT NULL DEFAULT FALSE,
    includes_pickup     BOOLEAN         NOT NULL DEFAULT FALSE,
    repair_limit        INT,
    contact_phone       VARCHAR(32),
    contact_email       VARCHAR(255),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_warranties_active ON warranties(active);
CREATE INDEX idx_warranties_type ON warranties(type);

-- Agregar FK opcional en products
ALTER TABLE products ADD COLUMN warranty_id VARCHAR(64);
ALTER TABLE products ADD CONSTRAINT fk_products_warranty
    FOREIGN KEY (warranty_id) REFERENCES warranties(id) ON DELETE SET NULL;

-- ============================================================
-- cj_tokens: Persistencia del access/refresh token de CJ Dropshipping
-- ============================================================

CREATE TABLE IF NOT EXISTS cj_tokens (
    id                      VARCHAR(32)   NOT NULL DEFAULT 'SINGLETON',
    access_token            TEXT,
    refresh_token           TEXT,
    access_token_expiry     TIMESTAMPTZ,
    refresh_token_expiry    TIMESTAMPTZ,
    last_token_request_time TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ,
    CONSTRAINT pk_cj_tokens PRIMARY KEY (id)
);

-- ============================================================
-- Ampliar product_type de varchar(10) a varchar(50)
-- ============================================================

ALTER TABLE products ALTER COLUMN product_type TYPE VARCHAR(50);

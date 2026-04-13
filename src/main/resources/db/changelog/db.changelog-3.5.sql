-- ============================================================
-- CJ API sync tracking: audit columns + tables
-- ============================================================

-- ── Sync-tracking timestamps on product_details ──────────────────────────────
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS product_synced_at  TIMESTAMPTZ;
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS inventory_synced_at TIMESTAMPTZ;
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS reviews_synced_at  TIMESTAMPTZ;

-- Indexes to help the scheduler find stale rows
-- (partial-index predicates cannot use NOW()/CURRENT_DATE as they are not IMMUTABLE,
--  so we index only on the IS NULL case; the scheduler filters the rest in-query)
CREATE INDEX IF NOT EXISTS idx_pd_inventory_sync_pending
    ON product_details (pid)
    WHERE inventory_synced_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pd_product_sync_pending
    ON product_details (pid)
    WHERE product_synced_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_pd_reviews_sync_pending
    ON product_details (pid)
    WHERE reviews_synced_at IS NULL;

-- ── Sync audit log ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sync_log (
    id              VARCHAR(64)   PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
    sync_type       VARCHAR(30)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'RUNNING',
    started_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    finished_at     TIMESTAMPTZ,
    total_items     INTEGER       NOT NULL DEFAULT 0,
    synced_items    INTEGER       NOT NULL DEFAULT 0,
    failed_items    INTEGER       NOT NULL DEFAULT 0,
    skipped_items   INTEGER       NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_sync_log_status   CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','FAILED')),
    CONSTRAINT chk_sync_log_type     CHECK (sync_type IN ('INVENTORY','PRODUCT_FULL','REVIEWS','CATEGORIES'))
);

CREATE INDEX IF NOT EXISTS idx_sync_log_type_status ON sync_log (sync_type, status);
CREATE INDEX IF NOT EXISTS idx_sync_log_started     ON sync_log (started_at DESC);

-- ── Sync failure registry ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sync_failure (
    id              VARCHAR(64)   PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
    sync_log_id     VARCHAR(64)   REFERENCES sync_log(id) ON DELETE SET NULL,
    entity_type     VARCHAR(30)   NOT NULL,
    entity_id       VARCHAR(128)  NOT NULL,
    error_code      VARCHAR(20),
    error_message   TEXT,
    retry_count     INTEGER       NOT NULL DEFAULT 0,
    max_retries     INTEGER       NOT NULL DEFAULT 3,
    next_retry_at   TIMESTAMPTZ,
    resolved        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sync_failure_unresolved ON sync_failure (entity_type, resolved, next_retry_at)
    WHERE resolved = FALSE;
CREATE INDEX IF NOT EXISTS idx_sync_failure_log ON sync_failure (sync_log_id);

-- ── CJ review import fields on reviews ────────────────────────────────────
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS external_review_id VARCHAR(64);
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS source             VARCHAR(30) NOT NULL DEFAULT 'USER';
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS country_code       VARCHAR(5);

CREATE UNIQUE INDEX IF NOT EXISTS idx_reviews_external_id
    ON reviews (external_review_id)
    WHERE external_review_id IS NOT NULL;

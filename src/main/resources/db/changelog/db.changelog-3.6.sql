-- =============================================================
-- Migration 3.6: Product Discovery Crawler tables
--
-- New tables for multi-strategy product discovery:
--   - discovered_pids: registry of PIDs found by the crawler
--   - discovery_state: persistent checkpoint per strategy
-- Also adds discovery_source column to product_details
-- =============================================================

-- 1. discovered_pids — each PID discovered by the crawler
CREATE TABLE IF NOT EXISTS discovered_pids (
    id            VARCHAR(36)  PRIMARY KEY DEFAULT gen_random_uuid(),
    pid           VARCHAR(200) NOT NULL UNIQUE,
    category_id   VARCHAR(200),
    keyword       VARCHAR(200),
    strategy      VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    name_en       VARCHAR(500),
    sell_price    VARCHAR(50),
    error_count   INT          NOT NULL DEFAULT 0,
    last_error    TEXT,
    discovered_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    synced_at     TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_discovered_pids_status ON discovered_pids(status);
CREATE INDEX IF NOT EXISTS idx_discovered_pids_pid ON discovered_pids(pid);
CREATE INDEX IF NOT EXISTS idx_discovered_pids_strategy ON discovered_pids(strategy);
CREATE INDEX IF NOT EXISTS idx_discovered_pids_discovered_at ON discovered_pids(discovered_at);

-- 2. discovery_state — persistent checkpoint for each strategy
CREATE TABLE IF NOT EXISTS discovery_state (
    strategy         VARCHAR(30) PRIMARY KEY,
    last_crawled_at  TIMESTAMP,
    last_category_id VARCHAR(200),
    last_keyword     VARCHAR(200),
    last_page        INT DEFAULT 0,
    total_discovered INT DEFAULT 0,
    last_run_at      TIMESTAMP,
    status           VARCHAR(20) DEFAULT 'IDLE',
    updated_at       TIMESTAMP DEFAULT NOW()
);

INSERT INTO discovery_state (strategy) VALUES ('BY_CATEGORY'), ('BY_KEYWORD'), ('BY_TIME')
ON CONFLICT (strategy) DO NOTHING;

-- 3. Add discovery_source column to product_details
ALTER TABLE product_details ADD COLUMN IF NOT EXISTS discovery_source VARCHAR(20);

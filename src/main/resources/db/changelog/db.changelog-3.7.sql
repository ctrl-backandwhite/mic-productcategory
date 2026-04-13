-- =============================================================
-- Migration 3.7: Add last_discovered_at to categories
--
-- Tracks when each category was last processed by the CJ
-- product discover flow. Enables incremental sync: prioritize
-- categories that have never been synced or are the oldest.
-- =============================================================

ALTER TABLE categories
ADD COLUMN IF NOT EXISTS last_discovered_at TIMESTAMPTZ;

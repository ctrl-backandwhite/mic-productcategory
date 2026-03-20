-- =============================================
-- Changeset 3: Add 'featured' column to categories
-- =============================================

ALTER TABLE categories ADD COLUMN featured BOOLEAN DEFAULT FALSE;

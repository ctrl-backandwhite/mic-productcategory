-- =============================================
-- Changeset 1.8: Add status (DRAFT/PUBLISHED) to products and product_detail_variants
-- =============================================

-- 1. Add status column to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 2. Add status column to product_detail_variants table
ALTER TABLE product_detail_variants ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 3. Create indexes for filtering by status
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_detail_variants_status ON product_detail_variants(status);

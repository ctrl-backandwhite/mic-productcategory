-- Add audit columns to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE products ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_by VARCHAR(120);

-- Add audit columns to product_variants table
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS updated_by VARCHAR(120);

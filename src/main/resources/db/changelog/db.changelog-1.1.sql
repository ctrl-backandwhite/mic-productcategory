-- Añadir columnas de auditoría que faltaban en la tabla categories
ALTER TABLE categories ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS created_by VARCHAR(120);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(120);

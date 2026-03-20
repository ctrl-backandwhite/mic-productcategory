-- =============================================
-- Changeset 1.7: Product Details model + cleanup
-- =============================================

-- 1. Fix categories.status default (was 'PUBLISHED', entity uses DRAFT)
ALTER TABLE categories ALTER COLUMN status SET DEFAULT 'DRAFT';

-- 2. Drop obsolete tables (replaced by product_detail_* tables)
DROP TABLE IF EXISTS product_variant_translations;
DROP TABLE IF EXISTS product_variants;

-- 3. Create product_details table
CREATE TABLE IF NOT EXISTS product_details (
    pid                VARCHAR(64) PRIMARY KEY,
    product_name_en    VARCHAR(500),
    product_sku        VARCHAR(64),
    big_image          TEXT,
    product_image      TEXT,
    product_image_set  TEXT,
    product_weight     VARCHAR(50),
    product_unit       VARCHAR(50),
    product_type       VARCHAR(50),
    category_id        VARCHAR(64),
    category_name      TEXT,
    entry_code         VARCHAR(50),
    entry_name_en      VARCHAR(255),
    material_name_en   TEXT,
    material_key       TEXT,
    packing_weight     VARCHAR(50),
    packing_name_en    TEXT,
    packing_key        TEXT,
    product_key_en     VARCHAR(255),
    product_pro_en     TEXT,
    sell_price         VARCHAR(50),
    description        TEXT,
    suggest_sell_price VARCHAR(50),
    listed_num         INT DEFAULT 0,
    status             VARCHAR(10),
    supplier_name      VARCHAR(255),
    supplier_id        VARCHAR(64),
    creater_time       TIMESTAMP,
    created_at         TIMESTAMP DEFAULT NOW(),
    updated_at         TIMESTAMP,
    created_by         VARCHAR(120),
    updated_by         VARCHAR(120)
);

-- 4. Create product_detail_translations table
CREATE TABLE IF NOT EXISTS product_detail_translations (
    pid          VARCHAR(64) NOT NULL REFERENCES product_details(pid) ON DELETE CASCADE,
    locale       VARCHAR(5)  NOT NULL,
    product_name VARCHAR(500),
    entry_name   VARCHAR(255),
    material_name TEXT,
    packing_name  TEXT,
    product_key  VARCHAR(255),
    product_pro  TEXT,
    PRIMARY KEY (pid, locale)
);

-- 5. Create product_detail_variants table
CREATE TABLE IF NOT EXISTS product_detail_variants (
    vid                   VARCHAR(64) PRIMARY KEY,
    pid                   VARCHAR(64)    NOT NULL REFERENCES product_details(pid) ON DELETE CASCADE,
    variant_name_en       VARCHAR(500),
    variant_sku           VARCHAR(64),
    variant_unit          VARCHAR(20),
    variant_key           VARCHAR(255),
    variant_image         TEXT,
    variant_length        NUMERIC(10,2),
    variant_width         NUMERIC(10,2),
    variant_height        NUMERIC(10,2),
    variant_volume        NUMERIC(10,2),
    variant_weight        NUMERIC(10,2),
    variant_sell_price    NUMERIC(10,2),
    variant_sug_sell_price NUMERIC(10,2),
    variant_standard      VARCHAR(255),
    create_time           TIMESTAMP,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(120),
    updated_by            VARCHAR(120)
);

-- 6. Create product_detail_variant_translations table
CREATE TABLE IF NOT EXISTS product_detail_variant_translations (
    vid          VARCHAR(64) NOT NULL REFERENCES product_detail_variants(vid) ON DELETE CASCADE,
    locale       VARCHAR(5)  NOT NULL,
    variant_name VARCHAR(500),
    PRIMARY KEY (vid, locale)
);

-- 7. Create product_detail_variant_inventories table
CREATE TABLE IF NOT EXISTS product_detail_variant_inventories (
    id                 BIGSERIAL PRIMARY KEY,
    vid                VARCHAR(64) NOT NULL REFERENCES product_detail_variants(vid) ON DELETE CASCADE,
    country_code       VARCHAR(5),
    total_inventory    INT,
    cj_inventory       INT,
    factory_inventory  INT,
    verified_warehouse INT
);

-- 8. Rename Hibernate-generated indexes to match entity definitions
DROP INDEX IF EXISTS idx_pdv_pid;
DROP INDEX IF EXISTS idx_pdvi_vid;
DROP INDEX IF EXISTS idx_pd_category;
DROP INDEX IF EXISTS idx_pd_sku;

-- 9. Create indexes matching entity @Index annotations
CREATE INDEX IF NOT EXISTS idx_detail_translations_locale     ON product_detail_translations(locale);
CREATE INDEX IF NOT EXISTS idx_detail_variants_pid            ON product_detail_variants(pid);
CREATE INDEX IF NOT EXISTS idx_detail_variants_sku            ON product_detail_variants(variant_sku);
CREATE INDEX IF NOT EXISTS idx_detail_variant_trans_locale     ON product_detail_variant_translations(locale);
CREATE INDEX IF NOT EXISTS idx_detail_inv_vid                  ON product_detail_variant_inventories(vid);

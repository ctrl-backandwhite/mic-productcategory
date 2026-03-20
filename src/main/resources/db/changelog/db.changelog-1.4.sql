CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    sku VARCHAR(64),
    category_id VARCHAR(64) REFERENCES categories(id),
    big_image TEXT,
    sell_price VARCHAR(50),
    product_type VARCHAR(10),
    listed_num INT DEFAULT 0,
    warehouse_inventory_num INT DEFAULT 0,
    is_video BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE product_translations (
    product_id VARCHAR(64) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (product_id, locale)
);

CREATE TABLE product_variants (
    vid VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_sku VARCHAR(64),
    variant_image TEXT,
    variant_key VARCHAR(255),
    variant_unit VARCHAR(20),
    variant_length NUMERIC(10,2),
    variant_width NUMERIC(10,2),
    variant_height NUMERIC(10,2),
    variant_weight NUMERIC(10,2),
    variant_sell_price NUMERIC(10,2),
    variant_sug_sell_price NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE product_variant_translations (
    variant_id VARCHAR(64) NOT NULL REFERENCES product_variants(vid) ON DELETE CASCADE,
    locale VARCHAR(5) NOT NULL,
    name VARCHAR(500) NOT NULL,
    PRIMARY KEY (variant_id, locale)
);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_product_translations_locale ON product_translations(locale);
CREATE INDEX idx_variants_product ON product_variants(product_id);
CREATE INDEX idx_variants_sku ON product_variants(variant_sku);
CREATE INDEX idx_variant_translations_locale ON product_variant_translations(locale);
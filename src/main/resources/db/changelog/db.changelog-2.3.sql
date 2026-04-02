-- liquibase formatted sql
-- changeset 13:media-assets
-- comment: Tabla de media assets para la biblioteca de medios

CREATE TABLE media_assets (
    id              VARCHAR(64)     PRIMARY KEY,
    filename        VARCHAR(512)    NOT NULL UNIQUE,
    original_name   VARCHAR(512)    NOT NULL,
    mime_type       VARCHAR(128)    NOT NULL,
    size_bytes      BIGINT          NOT NULL,
    url             VARCHAR(1024)   NOT NULL,
    thumbnail_url   VARCHAR(1024),
    category        VARCHAR(32)     NOT NULL DEFAULT 'GENERAL',
    tags            JSONB           DEFAULT '[]'::jsonb,
    alt             TEXT,
    width           INT,
    height          INT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_media_assets_category ON media_assets(category);
CREATE INDEX idx_media_assets_mime_type ON media_assets(mime_type);
CREATE INDEX idx_media_assets_tags ON media_assets USING GIN(tags);

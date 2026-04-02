-- ============================================================
-- Reviews: Reseñas de productos
-- ============================================================

CREATE TABLE IF NOT EXISTS reviews (
    id              VARCHAR(64)     PRIMARY KEY,
    product_id      VARCHAR(64)     NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id         VARCHAR(64),
    author_name     VARCHAR(255)    NOT NULL,
    rating          SMALLINT        NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title           VARCHAR(500),
    body            TEXT,
    verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    helpful_count   INT             NOT NULL DEFAULT 0,
    images          JSONB           DEFAULT '[]',
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(product_id, rating);

-- ============================================================
-- Review Helpful Votes: Votos de utilidad (idempotente por sesión)
-- ============================================================

CREATE TABLE IF NOT EXISTS review_helpful_votes (
    id              VARCHAR(64)     PRIMARY KEY,
    review_id       VARCHAR(64)     NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    session_id      VARCHAR(255)    NOT NULL,
    created_at      TIMESTAMP       DEFAULT NOW(),
    CONSTRAINT uq_review_helpful_vote UNIQUE (review_id, session_id)
);

CREATE INDEX IF NOT EXISTS idx_review_helpful_review ON review_helpful_votes(review_id);

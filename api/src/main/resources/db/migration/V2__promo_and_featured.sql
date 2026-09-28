ALTER TABLE products ADD COLUMN compare_at_price_tnd INT
    CHECK (compare_at_price_tnd IS NULL OR compare_at_price_tnd > price_tnd);

ALTER TABLE products ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_products_featured ON products (featured) WHERE featured = TRUE;

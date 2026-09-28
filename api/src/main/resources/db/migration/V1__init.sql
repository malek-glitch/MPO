CREATE TABLE admin_users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    slug      VARCHAR(120) NOT NULL UNIQUE,
    parent_id INT REFERENCES categories (id) ON DELETE RESTRICT,
    position  INT          NOT NULL DEFAULT 0,
    visible   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE products (
    id              SERIAL PRIMARY KEY,
    slug            VARCHAR(220) NOT NULL UNIQUE,
    category_id     INT          NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    is_used         BOOLEAN      NOT NULL DEFAULT FALSE,
    brand           VARCHAR(80)  NOT NULL,
    model           VARCHAR(150) NOT NULL,
    processor       VARCHAR(150),
    ram_gb          INT,
    storage_gb      INT,
    storage_type    VARCHAR(20),
    gpu             VARCHAR(150),
    screen_inches   NUMERIC(4, 1),
    os              VARCHAR(80),
    keyboard        VARCHAR(30),
    price_tnd       INT          NOT NULL CHECK (price_tnd >= 0),
    quantity        INT          NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    status          VARCHAR(20)  NOT NULL DEFAULT 'available'
                    CHECK (status IN ('available', 'sold', 'hidden')),
    description     TEXT,
    warranty_months INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now(),
    sold_at         TIMESTAMP
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_brand ON products (brand);
CREATE INDEX idx_products_price ON products (price_tnd);

CREATE TABLE used_details (
    product_id     INT PRIMARY KEY REFERENCES products (id) ON DELETE CASCADE,
    condition      VARCHAR(20) NOT NULL
                   CHECK (condition IN ('like_new', 'very_good', 'good', 'fair')),
    battery_health INT CHECK (battery_health BETWEEN 0 AND 100),
    defects        TEXT,
    accessories    TEXT
);

CREATE TABLE product_photos (
    id         SERIAL PRIMARY KEY,
    product_id INT          NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    object_key VARCHAR(255) NOT NULL,
    position   INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_photos_product ON product_photos (product_id);

CREATE TABLE store_settings (
    id                INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    name              VARCHAR(120) NOT NULL,
    logo_key          VARCHAR(255),
    whatsapp_number   VARCHAR(30)  NOT NULL DEFAULT '',
    phone             VARCHAR(30),
    address           TEXT,
    facebook_url      VARCHAR(255),
    instagram_url     VARCHAR(255),
    tiktok_url        VARCHAR(255),
    whatsapp_template TEXT,
    delivery_info     TEXT
);

CREATE TABLE contact_clicks (
    id         BIGSERIAL PRIMARY KEY,
    product_id INT REFERENCES products (id) ON DELETE CASCADE,
    clicked_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_clicks_date ON contact_clicks (clicked_at);

-- Default categories (French UI)
INSERT INTO categories (id, name, slug, parent_id, position) VALUES
    (1, 'PC portables',  'pc-portables',          NULL, 1),
    (2, 'MacBook',       'macbook',               NULL, 2),
    (3, 'PC de bureau',  'pc-de-bureau',          NULL, 3),
    (4, 'Standard',      'pc-portables-standard', 1,    1),
    (5, 'Pro',           'pc-portables-pro',      1,    2),
    (6, 'Gaming',        'pc-portables-gaming',   1,    3),
    (7, 'Air',           'macbook-air',           2,    1),
    (8, 'Pro',           'macbook-pro',           2,    2),
    (9, 'Neo',           'macbook-neo',           2,    3);

SELECT setval('categories_id_seq', 9);

INSERT INTO store_settings (id, name, whatsapp_template) VALUES
    (1, 'Ma boutique', 'Bonjour, je suis intéressé(e) par : {product} ({price} DT) — {link}');

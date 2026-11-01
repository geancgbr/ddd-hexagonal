CREATE TABLE products (
    id     UUID PRIMARY KEY,
    name   VARCHAR(200)   NOT NULL,
    price  NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    active BOOLEAN        NOT NULL
);

CREATE TABLE orders (
    id          UUID PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    status      VARCHAR(20)  NOT NULL
);

CREATE TABLE order_items (
    order_id   UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID           NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity BETWEEN 1 AND 99)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

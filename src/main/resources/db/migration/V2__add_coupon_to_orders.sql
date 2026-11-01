ALTER TABLE orders
    ADD COLUMN coupon_code       VARCHAR(50),
    ADD COLUMN coupon_percentage INTEGER CHECK (coupon_percentage BETWEEN 1 AND 50);

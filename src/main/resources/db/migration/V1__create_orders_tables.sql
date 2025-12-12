CREATE TABLE IF NOT EXISTS orders (
                                      id              BIGSERIAL PRIMARY KEY,
                                      user_id         BIGINT,
                                      status          VARCHAR(30) NOT NULL,
                                      total_price     NUMERIC(10,2) NOT NULL,
                                      created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                                      updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
                                           id              BIGSERIAL PRIMARY KEY,
                                           order_id        BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                           product_id      BIGINT NOT NULL,
                                           product_name    VARCHAR(255) NOT NULL,
                                           quantity        INT NOT NULL,
                                           unit_price      NUMERIC(10,2) NOT NULL,
                                           line_price      NUMERIC(10,2) NOT NULL
);

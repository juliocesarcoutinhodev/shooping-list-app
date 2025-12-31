CREATE TABLE tb_shopping_item
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopping_list_id BIGINT         NOT NULL,
    name             VARCHAR(100)   NOT NULL,
    normalized_name  VARCHAR(100)   NOT NULL,
    quantity         DECIMAL(10, 2) NOT NULL,
    unit             VARCHAR(20),
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_shopping_item_list
        FOREIGN KEY (shopping_list_id) REFERENCES tb_shopping_list (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_item_status CHECK (status IN ('PENDING', 'PURCHASED')),
    CONSTRAINT chk_item_quantity CHECK (quantity > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Índices
CREATE INDEX idx_shopping_item_list ON tb_shopping_item (shopping_list_id);
CREATE INDEX idx_shopping_item_status ON tb_shopping_item (status);
CREATE INDEX idx_shopping_item_normalized_name ON tb_shopping_item (normalized_name);


CREATE TABLE tb_shopping_list
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id    BIGINT       NOT NULL,
    title       VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_shopping_list_owner
        FOREIGN KEY (owner_id) REFERENCES tb_user (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Índices
CREATE INDEX idx_shopping_list_owner ON tb_shopping_list (owner_id);
CREATE INDEX idx_shopping_list_created_at ON tb_shopping_list (created_at);


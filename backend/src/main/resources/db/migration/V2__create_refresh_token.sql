CREATE TABLE tb_refresh_token
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    token_hash           VARCHAR(255) NOT NULL UNIQUE,
    expires_at           TIMESTAMP(6) NOT NULL,
    revoked_at           TIMESTAMP(6) NULL,
    replaced_by_token_id BIGINT       NULL,
    created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_used_at         TIMESTAMP(6) NULL,
    user_agent           VARCHAR(500) NULL,
    ip                   VARCHAR(45)  NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES tb_user (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_refresh_token_replaced_by
        FOREIGN KEY (replaced_by_token_id) REFERENCES tb_refresh_token (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Índices essenciais
CREATE UNIQUE INDEX idx_refresh_token_hash ON tb_refresh_token (token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON tb_refresh_token (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON tb_refresh_token (expires_at);
CREATE INDEX idx_refresh_tokens_revoked_at ON tb_refresh_token (revoked_at);
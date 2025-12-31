CREATE TABLE tb_user
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    name          VARCHAR(150) NOT NULL,
    password_hash TEXT,
    provider      VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT chk_provider CHECK (provider IN ('LOCAL', 'GOOGLE')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT chk_local_password CHECK (
        (provider = 'LOCAL' AND password_hash IS NOT NULL) OR
        (provider = 'GOOGLE' AND password_hash IS NULL)
        )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Índices
CREATE INDEX idx_users_email ON tb_user (email);
CREATE INDEX idx_users_status ON tb_user (status);
CREATE INDEX idx_users_provider ON tb_user (provider);
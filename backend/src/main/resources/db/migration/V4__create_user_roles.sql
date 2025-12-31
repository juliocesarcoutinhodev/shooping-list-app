-- Migration V4: Criar tabela de relacionamento user_roles
-- Autor: Sistema
-- Data: 2025-12-25
-- Descrição: Tabela de relacionamentoMany-to-Many entre usuários e roles

CREATE TABLE tb_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES tb_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id)
        REFERENCES tb_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para melhorar performance das queries
CREATE INDEX idx_user_role_user_id ON tb_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON tb_user_role(role_id);

-- Comentários da tabela
ALTER TABLE tb_user_role
    COMMENT = 'Tabela de relacionamento Many-to-Many entre usuários e roles';


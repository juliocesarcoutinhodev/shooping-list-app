-- Migration V3: Criar tabela de roles
-- Autor: Sistema
-- Data: 2025-12-25
-- Descrição: Tabela para armazenar roles (papéis) do sistema

CREATE TABLE tb_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índice para busca por nome (já é UNIQUE mas explicitamos para clareza)
CREATE INDEX idx_role_name ON tb_role(name);

-- Comentários das colunas
ALTER TABLE tb_role
    COMMENT = 'Tabela de roles (papéis) do sistema para controle de autorização';


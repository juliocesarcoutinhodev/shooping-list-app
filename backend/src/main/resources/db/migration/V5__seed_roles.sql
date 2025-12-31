-- Migration V5: Seed de roles padrão do sistema
-- Autor: Sistema
-- Data: 2025-12-25
-- Descrição: Insere roles padrão (USER e ADMIN) no sistema

INSERT INTO tb_role (name, description) VALUES
    ('USER', 'Usuário padrão com permissões básicas do sistema'),
    ('ADMIN', 'Administrador com permissões completas de gestão');

-- Validação: Verificar se as roles foram inseridas
-- SELECT * FROM tb_role;


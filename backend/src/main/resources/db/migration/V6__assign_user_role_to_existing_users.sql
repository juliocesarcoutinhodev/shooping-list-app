-- Migration V6: Atribuir role USER a usuários existentes
-- Autor: Sistema
-- Data: 2025-12-25
-- Descrição: Garante que todos os usuários existentes no sistema tenham pelo menos a role USER

-- Insere role USER para todos os usuários que ainda não possuem nenhuma role
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id
FROM tb_user u
CROSS JOIN tb_role r
WHERE r.name = 'USER'
AND NOT EXISTS (
    SELECT 1
    FROM tb_user_role ur
    WHERE ur.user_id = u.id
);

-- Validação: Verificar quantos usuários foram afetados
-- SELECT COUNT(*) as usuarios_com_role_user FROM tb_user_role ur
-- JOIN tb_role r ON ur.role_id = r.id
-- WHERE r.name = 'USER';


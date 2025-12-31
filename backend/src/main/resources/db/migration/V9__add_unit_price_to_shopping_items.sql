-- Adiciona coluna unit_price (preço unitário) na tabela de itens
-- Campo opcional (nullable) para permitir itens sem preço
ALTER TABLE tb_shopping_item
    ADD COLUMN unit_price DECIMAL(10, 2) NULL
    COMMENT 'Preço unitário do item (opcional)';

-- Índice para consultas por preço (opcional, mas útil para relatórios)
CREATE INDEX idx_shopping_item_unit_price ON tb_shopping_item (unit_price);




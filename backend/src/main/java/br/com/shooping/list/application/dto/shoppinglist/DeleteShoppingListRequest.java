package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para requisição de exclusão de lista de compras.
 * Contém apenas o ID da lista a ser excluída.
 * O ownerId virá do JWT e será passado separadamente pelo controller.
 */
public record DeleteShoppingListRequest(
        @NotNull(message = "ID da lista é obrigatório")
        Long listId
) {}


package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando um usuário tenta acessar uma lista de compras
 * que não pertence a ele.
 * Geralmente resulta em resposta HTTP 403 Forbidden.
 */
public class UnauthorizedShoppingListAccessException extends RuntimeException {

    public UnauthorizedShoppingListAccessException(Long listId) {
        super(String.format("Você não tem permissão para acessar esta lista: %d", listId));
    }

    public UnauthorizedShoppingListAccessException(String message) {
        super(message);
    }
}


package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando uma lista de compras não é encontrada.
 * Geralmente resulta em resposta HTTP 404 Not Found.
 */
public class ShoppingListNotFoundException extends RuntimeException {

    public ShoppingListNotFoundException(Long listId) {
        super(String.format("Lista de compras não encontrada: %d", listId));
    }

    public ShoppingListNotFoundException(String message) {
        super(message);
    }
}

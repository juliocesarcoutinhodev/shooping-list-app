package br.com.shooping.list.domain.shoppinglist;

/**
 * Exceção lançada quando se tenta acessar ou modificar um item que não existe na lista.
 */
public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long itemId) {
        super(String.format("Item com ID %d não encontrado nesta lista", itemId));
    }
}

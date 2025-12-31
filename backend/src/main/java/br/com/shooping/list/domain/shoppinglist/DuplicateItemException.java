package br.com.shooping.list.domain.shoppinglist;

/**
 * Exceção lançada quando se tenta adicionar um item com nome duplicado na mesma lista.
 * A comparação de nomes é case-insensitive para melhor experiência do usuário.
 */
public class DuplicateItemException extends RuntimeException {

    public DuplicateItemException(String itemName) {
        super(String.format("Item '%s' já existe nesta lista", itemName));
    }
}

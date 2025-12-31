package br.com.shooping.list.domain.shoppinglist;

/**
 * Exceção lançada quando se tenta adicionar mais itens do que o limite permitido por lista.
 * O limite atual é de 100 itens por lista.
 */
public class ListLimitExceededException extends RuntimeException {

    public ListLimitExceededException(int maxItems) {
        super(String.format("Lista não pode ter mais de %d itens", maxItems));
    }
}

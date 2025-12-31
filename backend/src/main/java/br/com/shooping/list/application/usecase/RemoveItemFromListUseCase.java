package br.com.shooping.list.application.usecase;

import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para remover item de uma lista de compras.
 *
 * Responsabilidades:
 * - Buscar lista existente
 * - Validar ownership (apenas dono pode remover itens)
 * - Delegar remoção ao domínio (ShoppingList.removeItem)
 * - Persistir alterações
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RemoveItemFromListUseCase {

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Remove um item de uma lista de compras.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista que contém o item
     * @param itemId ID do item a ser removido
     * @throws ShoppingListNotFoundException se lista não existir
     * @throws UnauthorizedShoppingListAccessException se usuário não for o dono
     * @throws ItemNotFoundException se item não existir na lista
     */
    @Transactional
    public void execute(Long ownerId, Long listId, Long itemId) {
        log.info("Removendo item da lista: listId={}, itemId={}, ownerId={}", listId, itemId, ownerId);

        // Buscar lista
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> {
                    log.warn("Lista não encontrada: listId={}", listId);
                    return new ShoppingListNotFoundException(listId);
                });

        // Validar ownership
        if (!list.isOwnedBy(ownerId)) {
            log.warn("Tentativa de acesso não autorizado: listId={}, ownerId={}, realOwnerId={}",
                    listId, ownerId, list.getOwnerId());
            throw new UnauthorizedShoppingListAccessException(listId);
        }

        // Delegar remoção ao domínio (valida existência do item)
        list.removeItem(itemId);

        // Persistir alterações
        shoppingListRepository.save(list);

        log.info("Item removido com sucesso: listId={}, itemId={}", listId, itemId);
    }
}


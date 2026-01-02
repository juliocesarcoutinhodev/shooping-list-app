package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.mapper.ShoppingListMapper;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para buscar detalhes de uma lista de compras por ID.
 *
 * Responsabilidades:
 * - Buscar lista por ID
 * - Validar ownership (apenas dono pode ver)
 * - Mapear para DTO de resposta via ShoppingListMapper (MapStruct) incluindo todos os itens
 * - Retornar resposta completa
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetShoppingListByIdUseCase {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper mapper;

    /**
     * Busca uma lista de compras específica por ID com todos os itens.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista a ser buscada
     * @return lista completa com todos os itens
     * @throws ShoppingListNotFoundException se lista não existir
     * @throws UnauthorizedShoppingListAccessException se usuário não for o dono
     */
    @Transactional(readOnly = true)
    public ShoppingListResponse execute(Long ownerId, Long listId) {
        log.info("Buscando lista de compras: listId={}, ownerId={}", listId, ownerId);

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

        log.debug("Lista encontrada: listId={}, itemsCount={}", listId, list.getItems().size());

        // Mapear para resposta via MapStruct (incluindo itens)
        return mapper.toResponse(list);
    }
}


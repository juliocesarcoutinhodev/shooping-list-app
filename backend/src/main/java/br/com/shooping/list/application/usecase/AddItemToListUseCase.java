package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.mapper.ShoppingListMapper;
import br.com.shooping.list.domain.shoppinglist.*;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para adicionar item em uma lista de compras.
 *
 * Responsabilidades:
 * - Buscar lista existente
 * - Validar ownership (apenas dono pode adicionar itens)
 * - Delegar criação ao domínio (ShoppingList.addItem)
 * - Persistir alterações
 * - Mapear resposta via ShoppingListMapper (MapStruct)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddItemToListUseCase {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper mapper;

    /**
     * Adiciona um novo item em uma lista de compras.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista onde adicionar o item
     * @param request dados do item (nome, quantidade, unidade)
     * @return item criado
     * @throws ShoppingListNotFoundException se lista não existir
     * @throws UnauthorizedShoppingListAccessException se usuário não for o dono
     * @throws DuplicateItemException se item com mesmo nome já existe
     * @throws ListLimitExceededException se lista atingiu limite de 100 itens
     */
    @Transactional
    public ItemResponse execute(Long ownerId, Long listId, AddItemRequest request) {
        log.info("Adicionando item na lista: listId={}, ownerId={}, itemName={}",
                listId, ownerId, request.name());

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

        // Delegar criação ao domínio (valida duplicatas e limite)
        ItemName itemName = ItemName.of(request.name());
        Quantity quantity = Quantity.of(request.quantity());
        ListItem item = list.addItem(itemName, quantity, request.unit(), request.unitPrice());

        // Persistir alterações (flush para gerar IDs)
        ShoppingList savedList = shoppingListRepository.save(list);

        // Buscar o item criado para ter o ID gerado
        ListItem savedItem = savedList.getItems().stream()
                .filter(i -> i.getName().equals(itemName))
                .findFirst()
                .orElse(item);

        log.info("Item adicionado com sucesso: listId={}, itemId={}, itemName={}",
                listId, savedItem.getId(), request.name());

        // Mapear para resposta via MapStruct
        return mapper.toItemResponse(savedItem);
    }
}


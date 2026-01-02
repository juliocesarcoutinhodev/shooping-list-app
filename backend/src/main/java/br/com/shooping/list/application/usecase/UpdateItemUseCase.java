package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
import br.com.shooping.list.application.mapper.ShoppingListMapper;
import br.com.shooping.list.domain.shoppinglist.*;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para atualizar item em uma lista de compras.
 *
 * Responsabilidades:
 * - Buscar lista existente
 * - Validar ownership (apenas dono pode atualizar itens)
 * - Validar que pelo menos um campo foi fornecido
 * - Delegar atualizações ao domínio condicionalmente
 * - Persistir alterações
 * - Retornar item atualizado
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateItemUseCase {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper mapper;

    /**
     * Atualiza um item de uma lista de compras.
     * Permite atualização parcial: apenas os campos fornecidos serão atualizados.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista que contém o item
     * @param itemId ID do item a ser atualizado
     * @param request dados a serem atualizados (nome, quantidade, unidade, status)
     * @return item atualizado
     * @throws ShoppingListNotFoundException se lista não existir
     * @throws UnauthorizedShoppingListAccessException se usuário não for o dono
     * @throws ItemNotFoundException se item não existir na lista
     * @throws IllegalArgumentException se nenhum campo for fornecido ou status inválido
     * @throws DuplicateItemException se novo nome conflitar com outro item
     */
    @Transactional
    public ItemResponse execute(Long ownerId, Long listId, Long itemId, UpdateItemRequest request) {
        log.info("Atualizando item: listId={}, itemId={}, ownerId={}", listId, itemId, ownerId);

        // Validar que pelo menos um campo foi fornecido
        if (!request.hasAtLeastOneField()) {
            log.warn("Tentativa de atualização sem campos: listId={}, itemId={}", listId, itemId);
            throw new IllegalArgumentException("Pelo menos um campo deve ser fornecido para atualização");
        }

        // Validar status se fornecido
        if (!request.isValidStatus()) {
            log.warn("Status inválido fornecido: status={}", request.status());
            throw new IllegalArgumentException("Status deve ser PENDING ou PURCHASED");
        }

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

        // Aplicar atualizações condicionalmente
        if (request.name() != null && !request.name().isBlank()) {
            log.debug("Atualizando nome do item: itemId={}, novoNome={}", itemId, request.name());
            list.updateItemName(itemId, ItemName.of(request.name()));
        }

        if (request.quantity() != null) {
            log.debug("Atualizando quantidade do item: itemId={}, novaQuantidade={}", itemId, request.quantity());
            list.updateItemQuantity(itemId, Quantity.of(request.quantity()));
        }

        if (request.unit() != null) {
            log.debug("Atualizando unidade do item: itemId={}", itemId);
            list.updateItemUnit(itemId, request.unit());
        }

        if (request.unitPrice() != null) {
            log.debug("Atualizando preço unitário do item: itemId={}, novoPreco={}", itemId, request.unitPrice());
            list.updateItemUnitPrice(itemId, request.unitPrice());
        }

        if (request.status() != null) {
            log.debug("Atualizando status do item: itemId={}, novoStatus={}", itemId, request.status());
            if ("PURCHASED".equals(request.status())) {
                list.markItemAsPurchased(itemId);
            } else {
                list.markItemAsPending(itemId);
            }
        }

        // Persistir alterações
        shoppingListRepository.save(list);

        // Buscar item atualizado para retornar
        ListItem updatedItem = list.findItemById(itemId);

        log.info("Item atualizado com sucesso: listId={}, itemId={}", listId, itemId);

        // Mapear para resposta via MapStruct
        return mapper.toItemResponse(updatedItem);
    }
}


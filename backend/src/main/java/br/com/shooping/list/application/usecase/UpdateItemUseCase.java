package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
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
            log.warn("Status inválido fornecido: status={}", request.getStatus());
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
        if (request.getName() != null && !request.getName().isBlank()) {
            log.debug("Atualizando nome do item: itemId={}, novoNome={}", itemId, request.getName());
            list.updateItemName(itemId, ItemName.of(request.getName()));
        }

        if (request.getQuantity() != null) {
            log.debug("Atualizando quantidade do item: itemId={}, novaQuantidade={}", itemId, request.getQuantity());
            list.updateItemQuantity(itemId, Quantity.of(request.getQuantity()));
        }

        if (request.getUnit() != null) {
            log.debug("Atualizando unidade do item: itemId={}", itemId);
            list.updateItemUnit(itemId, request.getUnit());
        }

        if (request.getUnitPrice() != null) {
            log.debug("Atualizando preço unitário do item: itemId={}, novoPreco={}", itemId, request.getUnitPrice());
            list.updateItemUnitPrice(itemId, request.getUnitPrice());
        }

        if (request.getStatus() != null) {
            log.debug("Atualizando status do item: itemId={}, novoStatus={}", itemId, request.getStatus());
            if ("PURCHASED".equals(request.getStatus())) {
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

        // Mapear para resposta
        return ItemResponse.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName().getValue())
                .quantity(updatedItem.getQuantity())
                .unit(updatedItem.getUnit())
                .unitPrice(updatedItem.getUnitPrice())
                .status(updatedItem.getStatus().name())
                .createdAt(updatedItem.getCreatedAt())
                .updatedAt(updatedItem.getUpdatedAt())
                .build();
    }
}


package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
import br.com.shooping.list.application.usecase.AddItemToListUseCase;
import br.com.shooping.list.application.usecase.RemoveItemFromListUseCase;
import br.com.shooping.list.application.usecase.UpdateItemUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de itens em listas de compras.
 * Base path: /api/v1/lists/{listId}/items
 *
 * Todos os endpoints deste controller requerem autenticação JWT.
 * Todas as operações validam que o usuário é o proprietário da lista.
 */
@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListItemController {

    private final AddItemToListUseCase addItemToListUseCase;
    private final UpdateItemUseCase updateItemUseCase;
    private final RemoveItemFromListUseCase removeItemFromListUseCase;

    /**
     * Adiciona um novo item em uma lista de compras.
     *
     * @param listId ID da lista onde adicionar o item
     * @param request dados do item (nome, quantidade, unidade)
     * @return item criado com ID gerado
     */
    @PostMapping("/{listId}/items")
    public ResponseEntity<ItemResponse> addItem(
            @PathVariable Long listId,
            @Valid @RequestBody AddItemRequest request) {

        log.info("Requisição recebida: POST /api/v1/lists/{}/items", listId);

        Long ownerId = extractOwnerId();
        log.debug("Adicionando item na lista: listId={}, ownerId={}, itemName={}",
                listId, ownerId, request.getName());

        ItemResponse response = addItemToListUseCase.execute(ownerId, listId, request);

        log.info("Item adicionado com sucesso: listId={}, itemId={}", listId, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Atualiza um item existente em uma lista de compras.
     * Atualização parcial: envia apenas os campos que deseja alterar.
     * Pode ser usado para toggle de status (PENDING ↔ PURCHASED).
     *
     * @param listId ID da lista que contém o item
     * @param itemId ID do item a ser atualizado
     * @param request campos a serem atualizados (nome, quantidade, unidade, status)
     * @return item atualizado
     */
    @PatchMapping("/{listId}/items/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long listId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request) {

        log.info("Requisição recebida: PATCH /api/v1/lists/{}/items/{}", listId, itemId);

        Long ownerId = extractOwnerId();
        log.debug("Atualizando item: listId={}, itemId={}, ownerId={}", listId, itemId, ownerId);

        ItemResponse response = updateItemUseCase.execute(ownerId, listId, itemId, request);

        log.info("Item atualizado com sucesso: listId={}, itemId={}", listId, itemId);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove um item de uma lista de compras.
     *
     * @param listId ID da lista que contém o item
     * @param itemId ID do item a ser removido
     * @return 204 No Content em caso de sucesso
     */
    @DeleteMapping("/{listId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long listId,
            @PathVariable Long itemId) {

        log.info("Requisição recebida: DELETE /api/v1/lists/{}/items/{}", listId, itemId);

        Long ownerId = extractOwnerId();
        log.debug("Removendo item: listId={}, itemId={}, ownerId={}", listId, itemId, ownerId);

        removeItemFromListUseCase.execute(ownerId, listId, itemId);

        log.info("Item removido com sucesso: listId={}, itemId={}", listId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrai o ID do usuário autenticado do SecurityContext.
     * O userId foi colocado no contexto pelo JwtAuthenticationFilter.
     *
     * @return ID do usuário autenticado
     */
    private Long extractOwnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        return Long.parseLong(userId);
    }
}


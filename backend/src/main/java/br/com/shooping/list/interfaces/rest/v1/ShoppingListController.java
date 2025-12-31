package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
import br.com.shooping.list.application.usecase.CreateShoppingListUseCase;
import br.com.shooping.list.application.usecase.DeleteShoppingListUseCase;
import br.com.shooping.list.application.usecase.GetMyShoppingListsUseCase;
import br.com.shooping.list.application.usecase.GetShoppingListByIdUseCase;
import br.com.shooping.list.application.usecase.UpdateShoppingListUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para operações de listas de compras.
 * Base path: /api/v1/lists
 *
 * Todos os endpoints deste controller requerem autenticação JWT.
 * O ownerId é extraído automaticamente do token JWT via SecurityContext.
 */
@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

    private final CreateShoppingListUseCase createShoppingListUseCase;
    private final GetMyShoppingListsUseCase getMyShoppingListsUseCase;
    private final GetShoppingListByIdUseCase getShoppingListByIdUseCase;
    private final UpdateShoppingListUseCase updateShoppingListUseCase;
    private final DeleteShoppingListUseCase deleteShoppingListUseCase;

    /**
     * Cria uma nova lista de compras para o usuário autenticado.
     *
     * @param request dados da lista (título e descrição opcional)
     * @return lista criada com ID gerado
     */
    @PostMapping
    public ResponseEntity<ShoppingListResponse> createList(@Valid @RequestBody CreateShoppingListRequest request) {
        log.info("Requisição recebida: POST /api/v1/lists");

        Long ownerId = extractOwnerId();
        log.debug("Criando lista de compras para usuário: ownerId={}, title={}", ownerId, request.getTitle());

        ShoppingListResponse response = createShoppingListUseCase.execute(ownerId, request);

        log.info("Lista criada com sucesso: listId={}, ownerId={}", response.getId(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorna todas as listas de compras do usuário autenticado.
     *
     * @return lista de resumos das listas de compras (pode ser vazia)
     */
    @GetMapping
    public ResponseEntity<List<ShoppingListSummaryResponse>> getMyLists() {
        log.info("Requisição recebida: GET /api/v1/lists");

        Long ownerId = extractOwnerId();
        log.debug("Buscando listas de compras do usuário: ownerId={}", ownerId);

        List<ShoppingListSummaryResponse> response = getMyShoppingListsUseCase.execute(ownerId);

        log.info("Listas retornadas com sucesso: ownerId={}, quantidade={}", ownerId, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna detalhes completos de uma lista de compras específica, incluindo todos os itens.
     * Valida que a lista pertence ao usuário autenticado.
     *
     * @param id ID da lista a ser buscada
     * @return lista completa com todos os itens
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> getListById(@PathVariable Long id) {
        log.info("Requisição recebida: GET /api/v1/lists/{}", id);

        Long ownerId = extractOwnerId();
        log.debug("Buscando lista de compras: listId={}, ownerId={}", id, ownerId);

        ShoppingListResponse response = getShoppingListByIdUseCase.execute(ownerId, id);

        log.info("Lista retornada com sucesso: listId={}, ownerId={}, itemsCount={}",
                id, ownerId, response.getItemsCount());
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza uma lista de compras existente (título e/ou descrição).
     * Atualização parcial: envia apenas os campos que deseja alterar.
     * Valida que a lista pertence ao usuário autenticado.
     *
     * @param id ID da lista a ser atualizada
     * @param request título e/ou descrição novos
     * @return lista atualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> updateList(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShoppingListRequest request
    ) {
        log.info("Requisição recebida: PATCH /api/v1/lists/{}", id);

        Long ownerId = extractOwnerId();
        log.debug("Atualizando lista: listId={}, ownerId={}", id, ownerId);

        ShoppingListResponse response = updateShoppingListUseCase.execute(ownerId, id, request);

        log.info("Lista atualizada com sucesso: listId={}, ownerId={}", id, ownerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta uma lista de compras.
     * Valida que a lista pertence ao usuário autenticado.
     * A exclusão é em cascata, removendo também todos os itens.
     *
     * @param id ID da lista a ser deletada
     * @return 204 No Content em caso de sucesso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteList(@PathVariable Long id) {
        log.info("Requisição recebida: DELETE /api/v1/lists/{}", id);

        Long ownerId = extractOwnerId();
        log.debug("Deletando lista: listId={}, ownerId={}", id, ownerId);

        deleteShoppingListUseCase.execute(ownerId, id);

        log.info("Lista deletada com sucesso: listId={}, ownerId={}", id, ownerId);
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


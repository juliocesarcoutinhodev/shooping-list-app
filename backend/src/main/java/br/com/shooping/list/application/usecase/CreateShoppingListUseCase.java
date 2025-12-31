package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para criar uma nova lista de compras.
 *
 * Responsabilidades:
 * - Validar dados de entrada (via Jakarta Validation)
 * - Delegar criação ao domínio (ShoppingList.create)
 * - Persistir via repositório
 * - Retornar resposta mapeada
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateShoppingListUseCase {

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Cria uma nova lista de compras para o usuário autenticado.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param request dados da lista (título e descrição)
     * @return lista criada com ID gerado
     */
    @Transactional
    public ShoppingListResponse execute(Long ownerId, CreateShoppingListRequest request) {
        log.info("Criando lista de compras: ownerId={}, title={}", ownerId, request.getTitle());

        // Delegar criação ao domínio (validações de negócio aplicadas)
        ShoppingList shoppingList = ShoppingList.create(
                ownerId,
                request.getTitle(),
                request.getDescription()
        );

        // Persistir
        ShoppingList savedList = shoppingListRepository.save(shoppingList);
        log.info("Lista criada com sucesso: id={}, ownerId={}", savedList.getId(), savedList.getOwnerId());

        // Mapear para resposta
        return mapToResponse(savedList);
    }

    /**
     * Mapeia entidade de domínio para DTO de resposta.
     */
    private ShoppingListResponse mapToResponse(ShoppingList list) {
        return ShoppingListResponse.builder()
                .id(list.getId())
                .ownerId(list.getOwnerId())
                .title(list.getTitle())
                .description(list.getDescription())
                .itemsCount(list.countTotalItems())
                .pendingItemsCount(list.countPendingItems())
                .purchasedItemsCount(list.countPurchasedItems())
                .createdAt(list.getCreatedAt())
                .updatedAt(list.getUpdatedAt())
                .build();
    }
}


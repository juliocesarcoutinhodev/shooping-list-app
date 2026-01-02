package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para buscar todas as listas de compras do usuário autenticado.
 *
 * Responsabilidades:
 * - Buscar listas do usuário via repositório
 * - Mapear para DTOs de resposta
 * - Retornar lista ordenada
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetMyShoppingListsUseCase {

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Busca todas as listas de compras do usuário autenticado.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @return lista de resumos das listas de compras (pode ser vazia)
     */
    @Transactional(readOnly = true)
    public List<ShoppingListSummaryResponse> execute(Long ownerId) {
        log.info("Buscando listas de compras do usuário: ownerId={}", ownerId);

        // Buscar listas do repositório
        List<ShoppingList> lists = shoppingListRepository.findByOwnerId(ownerId);
        log.debug("Encontradas {} listas para o usuário: ownerId={}", lists.size(), ownerId);

        // Mapear para DTOs
        return lists.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia entidade de domínio para DTO de resumo.
     */
    private ShoppingListSummaryResponse mapToSummaryResponse(ShoppingList list) {
        return new ShoppingListSummaryResponse(
                list.getId(),
                list.getTitle(),
                list.countTotalItems(),
                list.countPendingItems(),
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }
}


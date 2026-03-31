package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
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
 * Caso de uso para atualizar uma lista de compras (título e/ou descrição).
 *
 * Responsabilidades:
 * - Buscar lista existente
 * - Validar ownership (apenas dono pode atualizar)
 * - Validar que pelo menos um campo foi fornecido
 * - Delegar atualização ao domínio (ShoppingList.updateTitle/updateDescription)
 * - Persistir alterações
 * - Mapear resposta via ShoppingListMapper (MapStruct)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateShoppingListUseCase {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper mapper;

    /**
     * Atualiza uma lista de compras do usuário autenticado.
     * Permite atualização parcial: apenas os campos fornecidos serão atualizados.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista a ser atualizada (extraído da URL)
     * @param request dados com título e/ou descrição a serem atualizados
     * @return lista atualizada
     * @throws ShoppingListNotFoundException se lista não existir
     * @throws UnauthorizedShoppingListAccessException se usuário não for o dono
     * @throws IllegalArgumentException se nenhum campo for fornecido
     */
    @Transactional
    public ShoppingListResponse execute(Long ownerId, Long listId, UpdateShoppingListRequest request) {
        log.info("Atualizando lista de compras: listId={}, ownerId={}", listId, ownerId);

        // Validar que pelo menos um campo foi fornecido
        if (!request.hasAtLeastOneField()) {
            log.warn("Tentativa de atualização sem campos: listId={}", listId);
            throw new IllegalArgumentException("Pelo menos um campo (título ou descrição) deve ser fornecido para atualização");
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
        if (request.title() != null && !request.title().isBlank()) {
            log.debug("Atualizando título: listId={}, novoTitulo={}", listId, request.title());
            list.updateTitle(request.title());
        }

        if (request.description() != null) {
            log.debug("Atualizando descrição: listId={}", listId);
            list.updateDescription(request.description());
        }

        // Persistir alterações
        ShoppingList updatedList = shoppingListRepository.save(list);

        log.info("Lista atualizada com sucesso: listId={}, ownerId={}", listId, ownerId);

        // Mapear para resposta via MapStruct (sem itens)
        return mapper.toResponseWithoutItems(updatedList);
    }
}


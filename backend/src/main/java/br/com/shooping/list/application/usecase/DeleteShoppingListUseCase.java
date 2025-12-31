package br.com.shooping.list.application.usecase;

import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para deletar uma lista de compras.
 *
 * Responsabilidades:
 * - Validar existência da lista
 * - Validar ownership (apenas dono pode deletar)
 * - Deletar via repositório (cascata remove itens)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteShoppingListUseCase {

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Deleta uma lista de compras do usuário autenticado.
     * A exclusão é em cascata, removendo também todos os itens da lista.
     *
     * @param ownerId ID do usuário proprietário (extraído do JWT)
     * @param listId ID da lista a ser deletada
     * @throws ShoppingListNotFoundException se lista não existir ou não pertencer ao usuário
     */
    @Transactional
    public void execute(Long ownerId, Long listId) {
        log.info("Deletando lista de compras: listId={}, ownerId={}", listId, ownerId);

        // Validar existência e ownership em uma única query
        if (!shoppingListRepository.existsByIdAndOwnerId(listId, ownerId)) {
            log.warn("Lista não encontrada ou sem permissão: listId={}, ownerId={}", listId, ownerId);
            throw new ShoppingListNotFoundException(
                    String.format("Lista não encontrada ou você não tem permissão para deletá-la: %d", listId)
            );
        }

        // Deletar (cascata remove itens automaticamente)
        shoppingListRepository.deleteById(listId);
        log.info("Lista deletada com sucesso: listId={}", listId);
    }
}


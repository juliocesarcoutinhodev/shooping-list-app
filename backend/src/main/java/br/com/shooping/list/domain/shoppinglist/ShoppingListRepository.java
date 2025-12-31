package br.com.shooping.list.domain.shoppinglist;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para ShoppingList (Port - Clean Architecture).
 * Define as operações de persistência necessárias para o agregado ShoppingList,
 * sem depender de detalhes de infraestrutura (JPA, JDBC, etc).
 * A implementação concreta fica na camada infrastructure.
 */
public interface ShoppingListRepository {

    /**
     * Salva uma lista de compras (insert ou update).
     *
     * @param shoppingList lista a ser salva
     * @return lista salva com ID preenchido
     */
    ShoppingList save(ShoppingList shoppingList);

    /**
     * Busca uma lista por ID.
     *
     * @param id ID da lista
     * @return Optional com lista se encontrada, empty caso contrário
     */
    Optional<ShoppingList> findById(Long id);

    /**
     * Busca todas as listas de um usuário.
     *
     * @param ownerId ID do dono das listas
     * @return lista de shopping lists do usuário (pode ser vazia)
     */
    List<ShoppingList> findByOwnerId(Long ownerId);

    /**
     * Verifica se uma lista existe e pertence a um usuário.
     * Útil para validações de autorização.
     *
     * @param listId ID da lista
     * @param ownerId ID do dono
     * @return true se lista existe e pertence ao usuário, false caso contrário
     */
    boolean existsByIdAndOwnerId(Long listId, Long ownerId);

    /**
     * Remove uma lista por ID.
     *
     * @param id ID da lista a ser removida
     */
    void deleteById(Long id);

    /**
     * Remove todas as listas do repositório.
     * Útil para testes.
     */
    void deleteAll();
}

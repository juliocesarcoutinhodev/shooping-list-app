package br.com.shooping.list.infrastructure.persistence.shoppinglist;

import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementação JPA do repositório de ShoppingList (Adapter - Clean Architecture).
 * Implementa o contrato definido em ShoppingListRepository usando Spring Data JPA.
 * Esta é a camada de infraestrutura que depende dos detalhes técnicos (JPA),
 * mas implementa a interface definida no domínio (inversão de dependência - SOLID).
 *
 * Métodos CRUD básicos são herdados de JpaRepository mas precisam ser
 * declarados com @Override para satisfazer o contrato ShoppingListRepository.
 */
@Repository
public interface JpaShoppingListRepository extends JpaRepository<ShoppingList, Long>, ShoppingListRepository {

    /**
     * Salva uma lista de compras.
     * Implementação herdada de JpaRepository.
     */
    @Override
    ShoppingList save(ShoppingList shoppingList);

    /**
     * Busca uma lista por ID.
     * Implementação herdada de JpaRepository.
     */
    @Override
    Optional<ShoppingList> findById(Long id);

    /**
     * Busca todas as listas de um usuário.
     * Implementação customizada via query derivada do Spring Data JPA.
     */
    @Override
    List<ShoppingList> findByOwnerId(Long ownerId);

    /**
     * Verifica se uma lista existe e pertence a um usuário.
     * Implementação customizada via query derivada do Spring Data JPA.
     */
    @Override
    boolean existsByIdAndOwnerId(Long listId, Long ownerId);

    /**
     * Remove uma lista por ID.
     * Implementação herdada de JpaRepository.
     */
    @Override
    void deleteById(Long id);

    /**
     * Remove todas as listas.
     * Implementação herdada de JpaRepository.
     */
    @Override
    void deleteAll();
}




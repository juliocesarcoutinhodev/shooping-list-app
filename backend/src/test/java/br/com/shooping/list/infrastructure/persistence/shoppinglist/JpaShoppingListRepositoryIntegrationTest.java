package br.com.shooping.list.infrastructure.persistence.shoppinglist;

import br.com.shooping.list.AbstractIntegrationTest;
import br.com.shooping.list.domain.shoppinglist.*;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para JpaShoppingListRepository.
 * Valida persistência, relacionamentos e queries com banco MySQL real via Testcontainers.
 */
@DisplayName("JpaShoppingListRepository - Testes de Integração")
@Disabled("Testes de integração desabilitados por padrão. Habilite quando necessário.")
class JpaShoppingListRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        shoppingListRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.createLocalUser(
                "teste@email.com",
                "Usuario Teste",
                "hashedPassword"
        );
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Deve salvar lista de compras com sucesso")
    @Transactional
    void shouldSaveShoppingList() {
        // Arrange
        ShoppingList list = ShoppingList.create(
                testUser.getId(),
                "Lista da Feira",
                "Compras semanais"
        );

        // Act
        ShoppingList savedList = shoppingListRepository.save(list);

        // Assert
        assertThat(savedList.getId()).isNotNull();
        assertThat(savedList.getOwnerId()).isEqualTo(testUser.getId());
        assertThat(savedList.getTitle()).isEqualTo("Lista da Feira");
        assertThat(savedList.getDescription()).isEqualTo("Compras semanais");
        assertThat(savedList.getCreatedAt()).isNotNull();
        assertThat(savedList.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar lista por ID")
    @Transactional
    void shouldFindById() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Minha Lista", null);
        list = shoppingListRepository.save(list);

        // Act
        Optional<ShoppingList> found = shoppingListRepository.findById(list.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(list.getId());
        assertThat(found.get().getTitle()).isEqualTo("Minha Lista");
    }

    @Test
    @DisplayName("Deve buscar todas as listas de um usuário")
    @Transactional
    void shouldFindByOwnerId() {
        // Arrange
        ShoppingList list1 = ShoppingList.create(testUser.getId(), "Lista 1", null);
        ShoppingList list2 = ShoppingList.create(testUser.getId(), "Lista 2", null);
        ShoppingList list3 = ShoppingList.create(testUser.getId(), "Lista 3", null);

        shoppingListRepository.save(list1);
        shoppingListRepository.save(list2);
        shoppingListRepository.save(list3);

        // Act
        List<ShoppingList> lists = shoppingListRepository.findByOwnerId(testUser.getId());

        // Assert
        assertThat(lists).hasSize(3);
        assertThat(lists).extracting(ShoppingList::getTitle)
                .containsExactlyInAnyOrder("Lista 1", "Lista 2", "Lista 3");
    }

    @Test
    @DisplayName("Deve verificar se lista existe e pertence ao usuário")
    @Transactional
    void shouldCheckExistsByIdAndOwnerId() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Minha Lista", null);
        list = shoppingListRepository.save(list);

        // Act
        boolean exists = shoppingListRepository.existsByIdAndOwnerId(list.getId(), testUser.getId());
        boolean notExists = shoppingListRepository.existsByIdAndOwnerId(list.getId(), 999L);

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Deve deletar lista de compras")
    @Transactional
    void shouldDeleteShoppingList() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista Temporária", null);
        list = shoppingListRepository.save(list);
        Long listId = list.getId();

        // Act
        shoppingListRepository.deleteById(listId);

        // Assert
        Optional<ShoppingList> found = shoppingListRepository.findById(listId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve salvar lista com itens em cascata")
    @Transactional
    void shouldSaveListWithItemsInCascade() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista com Itens", null);

        ItemName arroz = ItemName.of("Arroz");
        Quantity quantidade = Quantity.of(2);
        list.addItem(arroz, quantidade, "kg", null);

        ItemName leite = ItemName.of("Leite");
        list.addItem(leite, Quantity.of(1), "litro", null);

        // Act
        ShoppingList savedList = shoppingListRepository.save(list);
        shoppingListRepository.flush();

        // Assert
        ShoppingList foundList = shoppingListRepository.findById(savedList.getId()).orElseThrow();
        assertThat(foundList.getItems()).hasSize(2);
        assertThat(foundList.getItems()).extracting(item -> item.getName().getValue())
                .containsExactlyInAnyOrder("Arroz", "Leite");
    }

    @Test
    @DisplayName("Deve deletar itens em cascata ao deletar lista")
    @Transactional
    void shouldDeleteItemsInCascade() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista para Deletar", null);
        list.addItem(ItemName.of("Item 1"), Quantity.of(1), null, null);
        list.addItem(ItemName.of("Item 2"), Quantity.of(2), null, null);

        list = shoppingListRepository.save(list);
        Long listId = list.getId();

        // Act
        shoppingListRepository.deleteById(listId);
        shoppingListRepository.flush();

        // Assert
        Optional<ShoppingList> found = shoppingListRepository.findById(listId);
        assertThat(found).isEmpty();
        // Se chegou aqui sem erro de FK constraint, itens foram deletados em cascata
    }

    @Test
    @DisplayName("Deve atualizar lista existente")
    @Transactional
    void shouldUpdateExistingList() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título Original", "Descrição Original");
        list = shoppingListRepository.save(list);
        Long listId = list.getId();

        // Act
        list.updateTitle("Título Atualizado");
        list.updateDescription("Descrição Atualizada");
        shoppingListRepository.save(list);

        // Assert
        ShoppingList updatedList = shoppingListRepository.findById(listId).orElseThrow();
        assertThat(updatedList.getTitle()).isEqualTo("Título Atualizado");
        assertThat(updatedList.getDescription()).isEqualTo("Descrição Atualizada");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem listas")
    @Transactional
    void shouldReturnEmptyListWhenUserHasNoLists() {
        // Act
        List<ShoppingList> lists = shoppingListRepository.findByOwnerId(testUser.getId());

        // Assert
        assertThat(lists).isEmpty();
    }

    @Test
    @DisplayName("Deve manter normalized_name ao salvar item")
    @Transactional
    void shouldPersistNormalizedName() {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista Teste", null);
        ItemName nome = ItemName.of("ARROZ Integral");
        list.addItem(nome, Quantity.of(1), "kg", null);

        // Act
        list = shoppingListRepository.save(list);
        shoppingListRepository.flush();

        // Assert
        ShoppingList foundList = shoppingListRepository.findById(list.getId()).orElseThrow();
        ListItem item = foundList.getItems().getFirst();
        assertThat(item.getName().getValue()).isEqualTo("ARROZ Integral");
        assertThat(item.getName().getNormalizedValue()).isEqualTo("arroz integral");
    }
}


package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GetMyShoppingListsUseCase.
 * Valida a busca de listas do usuário sem dependência de infraestrutura.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyShoppingListsUseCase - Testes Unitários")
class GetMyShoppingListsUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private GetMyShoppingListsUseCase getMyShoppingListsUseCase;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem listas")
    void shouldReturnEmptyListWhenUserHasNoLists() {
        // Arrange
        when(shoppingListRepository.findByOwnerId(ownerId)).thenReturn(List.of());

        // Act
        List<ShoppingListSummaryResponse> response = getMyShoppingListsUseCase.execute(ownerId);

        // Assert
        assertThat(response).isEmpty();
        verify(shoppingListRepository).findByOwnerId(ownerId);
    }

    @Test
    @DisplayName("Deve retornar todas as listas do usuário quando ele tem listas")
    void shouldReturnAllUserListsWhenUserHasLists() {
        // Arrange
        ShoppingList list1 = ShoppingList.create(ownerId, "Lista 1", "Descrição 1");
        ShoppingList list2 = ShoppingList.create(ownerId, "Lista 2", null);
        ShoppingList list3 = ShoppingList.create(ownerId, "Lista 3", "Descrição 3");

        setField(list1, "id", 1L);
        setField(list2, "id", 2L);
        setField(list3, "id", 3L);

        when(shoppingListRepository.findByOwnerId(ownerId))
                .thenReturn(Arrays.asList(list1, list2, list3));

        // Act
        List<ShoppingListSummaryResponse> response = getMyShoppingListsUseCase.execute(ownerId);

        // Assert
        assertThat(response).hasSize(3);

        ShoppingListSummaryResponse response1 = response.get(0);
        assertThat(response1.getId()).isEqualTo(1L);
        assertThat(response1.getTitle()).isEqualTo("Lista 1");
        assertThat(response1.getItemsCount()).isZero();
        assertThat(response1.getPendingItemsCount()).isZero();
        assertThat(response1.getCreatedAt()).isNotNull();
        assertThat(response1.getUpdatedAt()).isNotNull();

        ShoppingListSummaryResponse response2 = response.get(1);
        assertThat(response2.getId()).isEqualTo(2L);
        assertThat(response2.getTitle()).isEqualTo("Lista 2");

        ShoppingListSummaryResponse response3 = response.get(2);
        assertThat(response3.getId()).isEqualTo(3L);
        assertThat(response3.getTitle()).isEqualTo("Lista 3");

        verify(shoppingListRepository).findByOwnerId(ownerId);
    }

    @Test
    @DisplayName("Deve mapear corretamente contadores de itens")
    void shouldCorrectlyMapItemCounters() {
        // Arrange
        ShoppingList list = ShoppingList.create(ownerId, "Lista com Itens", "Test");
        setField(list, "id", 1L);

        when(shoppingListRepository.findByOwnerId(ownerId)).thenReturn(List.of(list));

        // Act
        List<ShoppingListSummaryResponse> response = getMyShoppingListsUseCase.execute(ownerId);

        // Assert
        assertThat(response).hasSize(1);
        ShoppingListSummaryResponse summary = response.get(0);
        assertThat(summary.getItemsCount()).isEqualTo(list.countTotalItems());
        assertThat(summary.getPendingItemsCount()).isEqualTo(list.countPendingItems());
    }

    /**
     * Usa reflexão para setar campo privado (simula ID gerado pelo banco).
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao setar campo via reflexão", e);
        }
    }
}


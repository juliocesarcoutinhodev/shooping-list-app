package br.com.shooping.list.application.usecase;

import br.com.shooping.list.domain.shoppinglist.*;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoveItemFromListUseCase - Testes Unitários")
class RemoveItemFromListUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private RemoveItemFromListUseCase removeItemFromListUseCase;

    private Long ownerId;
    private Long listId;
    private Long itemId;
    private ShoppingList existingList;
    private ListItem existingItem;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        listId = 10L;
        itemId = 1L;

        existingList = ShoppingList.create(ownerId, "Lista", null);
        setField(existingList, "id", listId);

        existingItem = existingList.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        setField(existingItem, "id", itemId);
    }

    @Test
    @DisplayName("Deve remover item com sucesso quando usuário é o dono")
    void shouldRemoveItemSuccessfullyWhenUserIsOwner() {
        // Arrange
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        removeItemFromListUseCase.execute(ownerId, listId, itemId);

        // Assert
        assertThat(existingList.getItems()).isEmpty();
        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista não existe")
    void shouldThrowExceptionWhenListDoesNotExist() {
        // Arrange
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> removeItemFromListUseCase.execute(ownerId, listId, itemId))
                .isInstanceOf(ShoppingListNotFoundException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono")
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long differentOwnerId = 999L;
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> removeItemFromListUseCase.execute(differentOwnerId, listId, itemId))
                .isInstanceOf(UnauthorizedShoppingListAccessException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não existe")
    void shouldThrowExceptionWhenItemDoesNotExist() {
        // Arrange
        Long nonExistentItemId = 999L;
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> removeItemFromListUseCase.execute(ownerId, listId, nonExistentItemId))
                .isInstanceOf(ItemNotFoundException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

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


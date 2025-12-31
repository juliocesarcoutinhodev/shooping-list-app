package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
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

/**
 * Testes unitários para AddItemToListUseCase.
 * Valida adição de itens com validação de ownership e regras de domínio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddItemToListUseCase - Testes Unitários")
class AddItemToListUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private AddItemToListUseCase addItemToListUseCase;

    private Long ownerId;
    private Long listId;
    private ShoppingList existingList;
    private AddItemRequest validRequest;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        listId = 10L;
        validRequest = new AddItemRequest("Arroz Integral", new BigDecimal("2.0"), "kg", BigDecimal.valueOf(35.00));

        existingList = ShoppingList.create(ownerId, "Lista da Feira", null);
        setField(existingList, "id", listId);
    }

    @Test
    @DisplayName("Deve adicionar item com sucesso quando usuário é o dono")
    void shouldAddItemSuccessfullyWhenUserIsOwner() {
        // Arrange
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = addItemToListUseCase.execute(ownerId, listId, validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Arroz Integral");
        assertThat(response.getQuantity()).isEqualByComparingTo(new BigDecimal("2.0"));
        assertThat(response.getUnit()).isEqualTo("kg");
        assertThat(response.getStatus()).isEqualTo("PENDING");

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista não existe")
    void shouldThrowExceptionWhenListDoesNotExist() {
        // Arrange
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addItemToListUseCase.execute(ownerId, listId, validRequest))
                .isInstanceOf(ShoppingListNotFoundException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono da lista")
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long differentOwnerId = 999L;
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> addItemToListUseCase.execute(differentOwnerId, listId, validRequest))
                .isInstanceOf(UnauthorizedShoppingListAccessException.class)
                .hasMessageContaining("não tem permissão");

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando item duplicado")
    void shouldThrowExceptionWhenDuplicateItem() {
        // Arrange
        // Adiciona item primeiro
        existingList.addItem(ItemName.of("Arroz Integral"), Quantity.of(BigDecimal.ONE), "kg", null);

        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> addItemToListUseCase.execute(ownerId, listId, validRequest))
                .isInstanceOf(DuplicateItemException.class)
                .hasMessageContaining("já existe");

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve adicionar item sem unidade")
    void shouldAddItemWithoutUnit() {
        // Arrange
        AddItemRequest requestWithoutUnit = new AddItemRequest("Banana", new BigDecimal("6"), null, BigDecimal.valueOf(30.00));
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = addItemToListUseCase.execute(ownerId, listId, requestWithoutUnit);

        // Assert
        assertThat(response.getName()).isEqualTo("Banana");
        assertThat(response.getUnit()).isNull();
        verify(shoppingListRepository).save(existingList);
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


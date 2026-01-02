package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
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
@DisplayName("UpdateItemUseCase - Testes Unitários")
class UpdateItemUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private UpdateItemUseCase updateItemUseCase;

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
    @DisplayName("Deve atualizar apenas nome quando fornecido")
    void shouldUpdateOnlyNameWhenProvided() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest("Feijão", null, null, null, null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = updateItemUseCase.execute(ownerId, listId, itemId, request);

        // Assert
        assertThat(response.name()).isEqualTo("Feijão");
        assertThat(response.quantity()).isEqualByComparingTo(BigDecimal.ONE);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve atualizar apenas quantidade quando fornecida")
    void shouldUpdateOnlyQuantityWhenProvided() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest(null, new BigDecimal("5"), null, null, null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = updateItemUseCase.execute(ownerId, listId, itemId, request);

        // Assert
        assertThat(response.quantity()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(response.name()).isEqualTo("Arroz");
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve atualizar status para PURCHASED")
    void shouldUpdateStatusToPurchased() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest(null, null, null, null, "PURCHASED");
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = updateItemUseCase.execute(ownerId, listId, itemId, request);

        // Assert
        assertThat(response.status()).isEqualTo("PURCHASED");
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos")
    void shouldUpdateMultipleFields() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest("Feijão Preto", new BigDecimal("2"), "pacote", null, "PURCHASED");
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemResponse response = updateItemUseCase.execute(ownerId, listId, itemId, request);

        // Assert
        assertThat(response.name()).isEqualTo("Feijão Preto");
        assertThat(response.quantity()).isEqualByComparingTo(new BigDecimal("2"));
        assertThat(response.unit()).isEqualTo("pacote");
        assertThat(response.status()).isEqualTo("PURCHASED");
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhum campo fornecido")
    void shouldThrowExceptionWhenNoFieldProvided() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest(null, null, null, null, null);

        // Act & Assert
        assertThatThrownBy(() -> updateItemUseCase.execute(ownerId, listId, itemId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pelo menos um campo");

        verify(shoppingListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando status inválido")
    void shouldThrowExceptionWhenInvalidStatus() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest(null, null, null, null, "INVALID");

        // Act & Assert
        assertThatThrownBy(() -> updateItemUseCase.execute(ownerId, listId, itemId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status deve ser");

        verify(shoppingListRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista não existe")
    void shouldThrowExceptionWhenListDoesNotExist() {
        // Arrange
        UpdateItemRequest request = new UpdateItemRequest("Novo Nome", null, null, null, null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateItemUseCase.execute(ownerId, listId, itemId, request))
                .isInstanceOf(ShoppingListNotFoundException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono")
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long differentOwnerId = 999L;
        UpdateItemRequest request = new UpdateItemRequest("Novo Nome", null, null, null, null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> updateItemUseCase.execute(differentOwnerId, listId, itemId, request))
                .isInstanceOf(UnauthorizedShoppingListAccessException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando item não existe")
    void shouldThrowExceptionWhenItemDoesNotExist() {
        // Arrange
        Long nonExistentItemId = 999L;
        UpdateItemRequest request = new UpdateItemRequest("Novo Nome", null, null, null, null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> updateItemUseCase.execute(ownerId, listId, nonExistentItemId, request))
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


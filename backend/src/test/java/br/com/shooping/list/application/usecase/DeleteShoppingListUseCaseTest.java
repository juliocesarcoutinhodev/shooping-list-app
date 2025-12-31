package br.com.shooping.list.application.usecase;

import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.persistence.shoppinglist.JpaShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DeleteShoppingListUseCase.
 * Valida a exclusão de listas com validação de ownership.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteShoppingListUseCase - Testes Unitários")
class DeleteShoppingListUseCaseTest {

    @Mock
    private JpaShoppingListRepository shoppingListRepository;

    @InjectMocks
    private DeleteShoppingListUseCase deleteShoppingListUseCase;

    private Long ownerId;
    private Long listId;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        listId = 10L;
    }

    @Test
    @DisplayName("Deve deletar lista com sucesso quando existe e pertence ao usuário")
    void shouldDeleteShoppingListSuccessfullyWhenExistsAndBelongsToUser() {
        // Arrange
        when(shoppingListRepository.existsByIdAndOwnerId(listId, ownerId)).thenReturn(true);
        doNothing().when(shoppingListRepository).deleteById(listId);

        // Act
        deleteShoppingListUseCase.execute(ownerId, listId);

        // Assert
        verify(shoppingListRepository).existsByIdAndOwnerId(listId, ownerId);
        verify(shoppingListRepository).deleteById(listId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista não existe")
    void shouldThrowExceptionWhenListDoesNotExist() {
        // Arrange
        when(shoppingListRepository.existsByIdAndOwnerId(listId, ownerId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> deleteShoppingListUseCase.execute(ownerId, listId))
                .isInstanceOf(ShoppingListNotFoundException.class)
                .hasMessageContaining("não encontrada");

        verify(shoppingListRepository).existsByIdAndOwnerId(listId, ownerId);
        verify(shoppingListRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono da lista")
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long differentOwnerId = 999L;
        when(shoppingListRepository.existsByIdAndOwnerId(listId, differentOwnerId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> deleteShoppingListUseCase.execute(differentOwnerId, listId))
                .isInstanceOf(ShoppingListNotFoundException.class)
                .hasMessageContaining("não tem permissão");

        verify(shoppingListRepository).existsByIdAndOwnerId(listId, differentOwnerId);
        verify(shoppingListRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve validar existência e ownership em uma única query")
    void shouldValidateExistenceAndOwnershipInSingleQuery() {
        // Arrange
        when(shoppingListRepository.existsByIdAndOwnerId(listId, ownerId)).thenReturn(true);
        doNothing().when(shoppingListRepository).deleteById(listId);

        // Act
        deleteShoppingListUseCase.execute(ownerId, listId);

        // Assert - Verifica que usa método otimizado ao invés de findById + isOwnedBy
        verify(shoppingListRepository, times(1)).existsByIdAndOwnerId(listId, ownerId);
        verify(shoppingListRepository, never()).findById(any());
    }
}


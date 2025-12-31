package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import br.com.shooping.list.infrastructure.exception.ShoppingListNotFoundException;
import br.com.shooping.list.infrastructure.exception.UnauthorizedShoppingListAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UpdateShoppingListUseCase.
 * Valida atualização de título e/ou descrição com validação de ownership.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateShoppingListUseCase - Testes Unitários")
class UpdateShoppingListUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private UpdateShoppingListUseCase updateShoppingListUseCase;

    private Long ownerId;
    private Long listId;
    private ShoppingList existingList;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        listId = 10L;

        existingList = ShoppingList.create(ownerId, "Título Antigo", "Descrição Antiga");
        setField(existingList, "id", listId);
    }

    @Test
    @DisplayName("Deve atualizar apenas título quando fornecido")
    void shouldUpdateOnlyTitleWhenProvided() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShoppingListResponse response = updateShoppingListUseCase.execute(ownerId, listId, request);

        // Assert
        assertThat(response.getTitle()).isEqualTo("Novo Título");
        assertThat(response.getDescription()).isEqualTo("Descrição Antiga"); // Não mudou
        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve atualizar apenas descrição quando fornecida")
    void shouldUpdateOnlyDescriptionWhenProvided() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest(null, "Nova Descrição");
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShoppingListResponse response = updateShoppingListUseCase.execute(ownerId, listId, request);

        // Assert
        assertThat(response.getTitle()).isEqualTo("Título Antigo"); // Não mudou
        assertThat(response.getDescription()).isEqualTo("Nova Descrição");
        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve atualizar título e descrição quando ambos fornecidos")
    void shouldUpdateBothTitleAndDescriptionWhenProvided() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", "Nova Descrição");
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShoppingListResponse response = updateShoppingListUseCase.execute(ownerId, listId, request);

        // Assert
        assertThat(response.getTitle()).isEqualTo("Novo Título");
        assertThat(response.getDescription()).isEqualTo("Nova Descrição");
        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository).save(existingList);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhum campo é fornecido")
    void shouldThrowExceptionWhenNoFieldProvided() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest(null, null);

        // Act & Assert
        assertThatThrownBy(() -> updateShoppingListUseCase.execute(ownerId, listId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pelo menos um campo");

        verify(shoppingListRepository, never()).findById(any());
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista não existe")
    void shouldThrowExceptionWhenListDoesNotExist() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateShoppingListUseCase.execute(ownerId, listId, request))
                .isInstanceOf(ShoppingListNotFoundException.class);

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é o dono da lista")
    void shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long differentOwnerId = 999L;
        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));

        // Act & Assert
        assertThatThrownBy(() -> updateShoppingListUseCase.execute(differentOwnerId, listId, request))
                .isInstanceOf(UnauthorizedShoppingListAccessException.class)
                .hasMessageContaining("não tem permissão");

        verify(shoppingListRepository).findById(listId);
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir remover descrição enviando string vazia")
    void shouldAllowRemovingDescriptionWithEmptyString() {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest(null, "");
        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(existingList));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ShoppingListResponse response = updateShoppingListUseCase.execute(ownerId, listId, request);

        // Assert
        assertThat(response.getDescription()).isNull();
        verify(shoppingListRepository).save(existingList);
    }

    /**
     * Usa reflexão para setar campo privado.
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


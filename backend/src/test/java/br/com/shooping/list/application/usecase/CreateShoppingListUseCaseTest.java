package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.shoppinglist.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CreateShoppingListUseCase.
 * Valida a criação de listas de compras sem dependência de infraestrutura.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateShoppingListUseCase - Testes Unitários")
class CreateShoppingListUseCaseTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private CreateShoppingListUseCase createShoppingListUseCase;

    private CreateShoppingListRequest validRequest;
    private Long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = 1L;
        validRequest = new CreateShoppingListRequest(
                "Lista da Feira",
                "Compras semanais do supermercado"
        );
    }

    @Test
    @DisplayName("Deve criar lista de compras com sucesso quando dados são válidos")
    void shouldCreateShoppingListSuccessfully() {
        // Arrange
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
            ShoppingList list = invocation.getArgument(0);
            setField(list, "id", 1L);
            return list;
        });

        // Act
        ShoppingListResponse response = createShoppingListUseCase.execute(ownerId, validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.title()).isEqualTo(validRequest.title());
        assertThat(response.description()).isEqualTo(validRequest.description());
        assertThat(response.itemsCount()).isZero();
        assertThat(response.pendingItemsCount()).isZero();
        assertThat(response.purchasedItemsCount()).isZero();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        // Verify
        verify(shoppingListRepository).save(any(ShoppingList.class));
    }

    @Test
    @DisplayName("Deve criar lista sem descrição quando descrição é null")
    void shouldCreateShoppingListWithoutDescription() {
        // Arrange
        CreateShoppingListRequest requestWithoutDescription = new CreateShoppingListRequest(
                "Lista Rápida",
                null
        );

        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
            ShoppingList list = invocation.getArgument(0);
            setField(list, "id", 2L);
            return list;
        });

        // Act
        ShoppingListResponse response = createShoppingListUseCase.execute(ownerId, requestWithoutDescription);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Lista Rápida");
        assertThat(response.description()).isNull();
    }

    @Test
    @DisplayName("Deve delegar validações ao domínio")
    void shouldDelegateValidationsToDomain() {
        // Arrange
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
            ShoppingList list = invocation.getArgument(0);
            setField(list, "id", 3L);
            return list;
        });

        // Act
        createShoppingListUseCase.execute(ownerId, validRequest);

        // Assert - Capturar lista salva
        ArgumentCaptor<ShoppingList> listCaptor = ArgumentCaptor.forClass(ShoppingList.class);
        verify(shoppingListRepository).save(listCaptor.capture());

        ShoppingList savedList = listCaptor.getValue();
        assertThat(savedList.getOwnerId()).isEqualTo(ownerId);
        assertThat(savedList.getTitle()).isEqualTo(validRequest.title());
        assertThat(savedList.getDescription()).isEqualTo(validRequest.description());
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


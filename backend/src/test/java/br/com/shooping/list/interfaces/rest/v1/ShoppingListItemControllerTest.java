package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
import br.com.shooping.list.domain.shoppinglist.*;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.persistence.shoppinglist.JpaShoppingListRepository;
import br.com.shooping.list.infrastructure.security.JwtService;
import br.com.shooping.list.test.support.TestDataSetup;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração end-to-end para ShoppingListItemController.
 * Valida todos os endpoints de gerenciamento de itens com autenticação JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ShoppingListItemController - Testes de Integração")
class ShoppingListItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDataSetup testDataSetup;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        shoppingListRepository.deleteAll();
        userRepository.deleteAll();
        testDataSetup.createDefaultRoles();

        testUser = User.createLocalUser("test@email.com", "Test User", "hashedPassword");
        testUser = userRepository.save(testUser);

        validToken = jwtService.generateAccessToken(testUser);
    }

    // ==================== POST /api/v1/lists/{listId}/items ====================

    @Test
    @DisplayName("POST - Deve adicionar item com sucesso")
    void shouldAddItemSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista da Feira", null);
        list = shoppingListRepository.save(list);

        AddItemRequest request = new AddItemRequest("Arroz Integral", new BigDecimal("2.0"), "kg", BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + list.getId() + "/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Arroz Integral")))
                .andExpect(jsonPath("$.quantity").value(2.0))
                .andExpect(jsonPath("$.unit", is("kg")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        // Verify
        ShoppingList updated = shoppingListRepository.findById(list.getId()).orElseThrow();
        assertThat(updated.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("POST - Deve adicionar item sem unidade")
    void shouldAddItemWithoutUnit() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        AddItemRequest request = new AddItemRequest("Banana", new BigDecimal("6"), null, BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + list.getId() + "/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Banana")))
                .andExpect(jsonPath("$.unit").doesNotExist());
    }

    @Test
    @DisplayName("POST - Deve retornar 400 quando nome é inválido")
    void shouldReturn400WhenNameIsInvalid() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        AddItemRequest request = new AddItemRequest("ab", new BigDecimal("1"), null, BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + list.getId() + "/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST - Deve retornar 400 quando item duplicado")
    void shouldReturn400WhenDuplicateItem() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        AddItemRequest request = new AddItemRequest("Arroz", new BigDecimal("2"), "kg", BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + list.getId() + "/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST - Deve retornar 404 quando lista não existe")
    void shouldReturn404WhenListNotFound() throws Exception {
        // Arrange
        AddItemRequest request = new AddItemRequest("Arroz", new BigDecimal("1"), "kg", BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/999/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST - Deve retornar 403 quando lista pertence a outro usuário")
    void shouldReturn403WhenAddingToAnotherUserList() throws Exception {
        // Arrange
        User anotherUser = User.createLocalUser("another@email.com", "Another", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro", null);
        otherList = shoppingListRepository.save(otherList);

        AddItemRequest request = new AddItemRequest("Arroz", new BigDecimal("1"), "kg", BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + otherList.getId() + "/items")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenAddingWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        AddItemRequest request = new AddItemRequest("Arroz", new BigDecimal("1"), "kg", BigDecimal.valueOf(35.00));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists/" + list.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PATCH /api/v1/lists/{listId}/items/{itemId} ====================

    @Test
    @DisplayName("PATCH - Deve atualizar nome do item")
    void shouldUpdateItemName() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest("Feijão", null, null, null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Feijão")));
    }

    @Test
    @DisplayName("PATCH - Deve atualizar quantidade do item")
    void shouldUpdateItemQuantity() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest(null, new BigDecimal("5"), null, null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    @DisplayName("PATCH - Deve marcar item como comprado")
    void shouldMarkItemAsPurchased() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest(null, null, null, null, "PURCHASED");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PURCHASED")));
    }

    @Test
    @DisplayName("PATCH - Deve atualizar múltiplos campos")
    void shouldUpdateMultipleFields() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest("Feijão Preto", new BigDecimal("3"), "pacote", BigDecimal.valueOf(30.00), "PURCHASED");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Feijão Preto")))
                .andExpect(jsonPath("$.quantity", is(3)))
                .andExpect(jsonPath("$.unit", is("pacote")))
                .andExpect(jsonPath("$.status", is("PURCHASED")));
    }

    @Test
    @DisplayName("PATCH - Deve retornar 400 quando nenhum campo fornecido")
    void shouldReturn400WhenNoFieldProvided() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest(null, null, null, null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH - Deve retornar 404 quando item não existe")
    void shouldReturn404WhenItemNotFound() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest("Novo Nome", null, null, null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenUpdatingWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        UpdateItemRequest request = new UpdateItemRequest("Novo Nome", null, null, null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DELETE /api/v1/lists/{listId}/items/{itemId} ====================

    @Test
    @DisplayName("DELETE - Deve remover item com sucesso")
    void shouldRemoveItemSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + list.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        // Verify
        ShoppingList updated = shoppingListRepository.findById(list.getId()).orElseThrow();
        assertThat(updated.getItems()).isEmpty();
    }

    @Test
    @DisplayName("DELETE - Deve retornar 404 quando item não existe")
    void shouldReturn404WhenRemovingNonExistentItem() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + list.getId() + "/items/999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE - Deve retornar 403 quando lista pertence a outro usuário")
    void shouldReturn403WhenRemovingFromAnotherUserList() throws Exception {
        // Arrange
        User anotherUser = User.createLocalUser("another@email.com", "Another", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro", null);
        ListItem item = otherList.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        otherList = shoppingListRepository.save(otherList);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + otherList.getId() + "/items/" + item.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenRemovingWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(BigDecimal.ONE), "kg", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + list.getId() + "/items/" + item.getId()))
                .andExpect(status().isUnauthorized());
    }
}


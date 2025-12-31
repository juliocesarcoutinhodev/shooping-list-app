package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
import br.com.shooping.list.domain.shoppinglist.ItemName;
import br.com.shooping.list.domain.shoppinglist.Quantity;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import br.com.shooping.list.domain.user.User;

import java.math.BigDecimal;
import br.com.shooping.list.infrastructure.persistence.shoppinglist.JpaShoppingListRepository;
import br.com.shooping.list.infrastructure.security.JwtService;
import br.com.shooping.list.domain.user.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração end-to-end para ShoppingListController.
 * Valida todos os endpoints de listas de compras com autenticação JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ShoppingListController - Testes de Integração")
class ShoppingListControllerTest {

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

        // Cria usuário de teste
        testUser = User.createLocalUser("test@email.com", "Test User", "hashedPassword");
        testUser = userRepository.save(testUser);

        // Gera token JWT válido para o usuário
        validToken = jwtService.generateAccessToken(testUser);
    }

    // ==================== POST /api/v1/lists ====================

    @Test
    @DisplayName("POST /api/v1/lists - Deve criar lista com sucesso")
    void shouldCreateListSuccessfully() throws Exception {
        // Arrange
        CreateShoppingListRequest request = new CreateShoppingListRequest(
                "Lista da Feira",
                "Compras semanais"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.ownerId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Lista da Feira")))
                .andExpect(jsonPath("$.description", is("Compras semanais")))
                .andExpect(jsonPath("$.itemsCount", is(0)))
                .andExpect(jsonPath("$.pendingItemsCount", is(0)))
                .andExpect(jsonPath("$.purchasedItemsCount", is(0)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        // Verify
        assertThat(shoppingListRepository.findByOwnerId(testUser.getId())).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/lists - Deve criar lista sem descrição")
    void shouldCreateListWithoutDescription() throws Exception {
        // Arrange
        CreateShoppingListRequest request = new CreateShoppingListRequest(
                "Lista Rápida",
                null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Lista Rápida")))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/lists - Deve retornar 400 quando título está vazio")
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        // Arrange
        CreateShoppingListRequest request = new CreateShoppingListRequest(
                "",
                "Descrição"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/lists - Deve retornar 400 quando título é muito curto")
    void shouldReturn400WhenTitleIsTooShort() throws Exception {
        // Arrange
        CreateShoppingListRequest request = new CreateShoppingListRequest(
                "ab", // 2 caracteres (mínimo é 3)
                null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/lists - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenCreatingWithoutToken() throws Exception {
        // Arrange
        CreateShoppingListRequest request = new CreateShoppingListRequest(
                "Lista Teste",
                null
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/v1/lists ====================

    @Test
    @DisplayName("GET /api/v1/lists - Deve retornar lista vazia quando usuário não tem listas")
    void shouldReturnEmptyListWhenUserHasNoLists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/lists - Deve retornar todas as listas do usuário")
    void shouldReturnAllUserLists() throws Exception {
        // Arrange - Criar 3 listas
        ShoppingList list1 = ShoppingList.create(testUser.getId(), "Lista 1", null);
        ShoppingList list2 = ShoppingList.create(testUser.getId(), "Lista 2", "Descrição 2");
        ShoppingList list3 = ShoppingList.create(testUser.getId(), "Lista 3", null);

        shoppingListRepository.save(list1);
        shoppingListRepository.save(list2);
        shoppingListRepository.save(list3);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Lista 1", "Lista 2", "Lista 3")));
    }

    @Test
    @DisplayName("GET /api/v1/lists - Deve retornar apenas listas do usuário autenticado")
    void shouldReturnOnlyAuthenticatedUserLists() throws Exception {
        // Arrange - Criar outro usuário com suas listas
        User anotherUser = User.createLocalUser("another@email.com", "Another User", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList myList = ShoppingList.create(testUser.getId(), "Minha Lista", null);
        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro Usuário", null);

        shoppingListRepository.save(myList);
        shoppingListRepository.save(otherList);

        // Act & Assert - Deve retornar apenas minha lista
        mockMvc.perform(get("/api/v1/lists")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Minha Lista")));
    }

    @Test
    @DisplayName("GET /api/v1/lists - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenListingWithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/lists"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/v1/lists/{id} ====================

    @Test
    @DisplayName("GET /api/v1/lists/{id} - Deve retornar lista com itens com sucesso")
    void shouldGetListByIdWithItemsSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista da Feira", "Compras semanais");
        list.addItem(ItemName.of("Arroz"), Quantity.of(new BigDecimal("2.0")), "kg", null);
        list.addItem(ItemName.of("Feijão"), Quantity.of(new BigDecimal("1.0")), "kg", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(list.getId().intValue())))
                .andExpect(jsonPath("$.ownerId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Lista da Feira")))
                .andExpect(jsonPath("$.description", is("Compras semanais")))
                .andExpect(jsonPath("$.items", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].name", is("Arroz")))
                .andExpect(jsonPath("$.items[0].quantity").value(2.0))
                .andExpect(jsonPath("$.items[0].unit", is("kg")))
                .andExpect(jsonPath("$.items[0].status", is("PENDING")))
                .andExpect(jsonPath("$.items[1].name", is("Feijão")))
                .andExpect(jsonPath("$.itemsCount", is(2)))
                .andExpect(jsonPath("$.pendingItemsCount", is(2)))
                .andExpect(jsonPath("$.purchasedItemsCount", is(0)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/lists/{id} - Deve retornar lista vazia (sem itens)")
    void shouldGetListByIdWithoutItems() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista Vazia", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(list.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Lista Vazia")))
                .andExpect(jsonPath("$.items", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.itemsCount", is(0)))
                .andExpect(jsonPath("$.pendingItemsCount", is(0)))
                .andExpect(jsonPath("$.purchasedItemsCount", is(0)));
    }

    @Test
    @DisplayName("GET /api/v1/lists/{id} - Deve retornar 404 quando lista não existe")
    void shouldReturn404WhenListNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/lists/99999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/lists/{id} - Deve retornar 403 quando lista pertence a outro usuário")
    void shouldReturn403WhenListBelongsToAnotherUser() throws Exception {
        // Arrange - Criar outro usuário com lista
        User anotherUser = User.createLocalUser("another@email.com", "Another User", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro", null);
        otherList = shoppingListRepository.save(otherList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lists/" + otherList.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/lists/{id} - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenGettingByIdWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lists/" + list.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PATCH /api/v1/lists/{id} ====================

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve atualizar título com sucesso")
    void shouldUpdateTitleSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título Original", "Descrição");
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(list.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Novo Título")))
                .andExpect(jsonPath("$.description", is("Descrição")));

        // Verify
        ShoppingList updated = shoppingListRepository.findById(list.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Novo Título");
        assertThat(updated.getDescription()).isEqualTo("Descrição");
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve atualizar descrição com sucesso")
    void shouldUpdateDescriptionSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título", "Descrição Antiga");
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest(null, "Nova Descrição");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Título")))
                .andExpect(jsonPath("$.description", is("Nova Descrição")));
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve atualizar título e descrição juntos")
    void shouldUpdateBothTitleAndDescription() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título Antigo", "Descrição Antiga");
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", "Nova Descrição");

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Novo Título")))
                .andExpect(jsonPath("$.description", is("Nova Descrição")));
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve retornar 400 quando nenhum campo é fornecido")
    void shouldReturn400WhenNoFieldProvided() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título", null);
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest(null, null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve retornar 404 quando lista não existe")
    void shouldReturn404WhenUpdatingNonExistentList() throws Exception {
        // Arrange
        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/999")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve retornar 403 quando lista pertence a outro usuário")
    void shouldReturn403WhenUpdatingAnotherUserList() throws Exception {
        // Arrange - Criar outro usuário e sua lista
        User anotherUser = User.createLocalUser("another@email.com", "Another User", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro", null);
        otherList = shoppingListRepository.save(otherList);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Tentando Atualizar", null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + otherList.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve retornar 400 quando título é inválido")
    void shouldReturn400WhenTitleIsInvalid() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título Original", null);
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("ab", null); // Muito curto

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/lists/{id} - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenUpdatingWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Título", null);
        list = shoppingListRepository.save(list);

        UpdateShoppingListRequest request = new UpdateShoppingListRequest("Novo Título", null);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/lists/" + list.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DELETE /api/v1/lists/{id} ====================

    @Test
    @DisplayName("DELETE /api/v1/lists/{id} - Deve deletar lista com sucesso")
    void shouldDeleteListSuccessfully() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista para Deletar", null);
        list = shoppingListRepository.save(list);
        Long listId = list.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + listId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        // Verify
        assertThat(shoppingListRepository.findById(listId)).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/lists/{id} - Deve retornar 404 quando lista não existe")
    void shouldReturn404WhenDeletingNonExistentList() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/999")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/lists/{id} - Deve retornar 403 quando lista pertence a outro usuário")
    void shouldReturn403WhenDeletingAnotherUserList() throws Exception {
        // Arrange - Criar outro usuário e sua lista
        User anotherUser = User.createLocalUser("another@email.com", "Another User", "hash");
        anotherUser = userRepository.save(anotherUser);

        ShoppingList otherList = ShoppingList.create(anotherUser.getId(), "Lista de Outro", null);
        otherList = shoppingListRepository.save(otherList);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + otherList.getId())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound()); // 404 porque não encontra lista do usuário autenticado

        // Verify - Lista não foi deletada
        assertThat(shoppingListRepository.findById(otherList.getId())).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/v1/lists/{id} - Deve retornar 401 sem token JWT")
    void shouldReturn401WhenDeletingWithoutToken() throws Exception {
        // Arrange
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista", null);
        list = shoppingListRepository.save(list);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lists/" + list.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/lists/{id} - Deve deletar itens em cascata")
    void shouldDeleteItemsInCascade() throws Exception {
        // Arrange - Criar lista com itens
        ShoppingList list = ShoppingList.create(testUser.getId(), "Lista com Itens", null);
        list = shoppingListRepository.save(list);
        Long listId = list.getId();

        // Act
        mockMvc.perform(delete("/api/v1/lists/" + listId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        // Verify - Lista e itens foram deletados
        assertThat(shoppingListRepository.findById(listId)).isEmpty();
    }
}


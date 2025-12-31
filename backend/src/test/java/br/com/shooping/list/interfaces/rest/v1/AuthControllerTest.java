package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.auth.RegisterRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Testes de Integração")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataSetup testDataSetup;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testDataSetup.createDefaultRoles();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve registrar usuário com sucesso")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "novo@email.com",
                "Novo Usuário",
                "senha@123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("novo@email.com")))
                .andExpect(jsonPath("$.name", is("Novo Usuário")))
                .andExpect(jsonPath("$.provider", is("LOCAL")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // Verify
        assertThat(userRepository.findByEmail("novo@email.com")).isPresent();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 409 quando email já existe")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // Arrange - Criar usuário primeiro
        RegisterRequest firstRequest = new RegisterRequest(
                "duplicado@email.com",
                "Primeiro Usuário",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // Tentar criar com mesmo email
        RegisterRequest duplicateRequest = new RegisterRequest(
                "duplicado@email.com",
                "Segundo Usuário",
                "outraSenha@456"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("duplicado@email.com")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 400 quando email é inválido")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "email-invalido",
                "Teste User",
                "senha@123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field", is("email")))
                .andExpect(jsonPath("$.details[0].message", containsString("válido")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 400 quando senha é muito curta")
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "teste@email.com",
                "Teste User",
                "123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field", is("password")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve retornar 400 quando campos são nulos")
    void shouldReturn400WhenFieldsAreNull() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(null, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasSize(3)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Deve criptografar senha antes de salvar")
    void shouldEncryptPasswordBeforeSaving() throws Exception {
        // Arrange
        String plainPassword = "senha@123";
        RegisterRequest request = new RegisterRequest(
                "criptografado@email.com",
                "Teste User",
                plainPassword
        );

        // Act
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Assert
        var user = userRepository.findByEmail("criptografado@email.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo(plainPassword);
        assertThat(user.getPasswordHash()).startsWith("$2a$"); // BCrypt hash
    }
}


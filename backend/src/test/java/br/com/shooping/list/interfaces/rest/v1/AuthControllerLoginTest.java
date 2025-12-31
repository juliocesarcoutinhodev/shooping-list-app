package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.auth.LoginRequest;
import br.com.shooping.list.application.dto.auth.RegisterRequest;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
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
 * Testes de integração para endpoint de Login no AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Login - Testes de Integração")
class AuthControllerLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestDataSetup testDataSetup;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        testDataSetup.createDefaultRoles();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange - Primeiro registrar um usuário
        RegisterRequest registerRequest = new RegisterRequest(
                "login@email.com",
                "Usuario Teste",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("login@email.com", "senha@123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .header("User-Agent", "Mozilla/5.0")
                        .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.expiresIn", notNullValue()))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)));

        // Verify - Refresh token deve estar persistido
        var persistedTokens = refreshTokenRepository.findAll();
        assertThat(persistedTokens).isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar access token JWT válido")
    void shouldReturnValidJwtAccessToken() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "jwt@email.com",
                "JWT User",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("jwt@email.com", "senha@123");

        // Act & Assert
        String responseContent = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Validar que o access token tem formato JWT (3 partes separadas por ponto)
        var response = objectMapper.readTree(responseContent);
        String accessToken = response.get("accessToken").asText();
        assertThat(accessToken).contains(".");
        assertThat(accessToken.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar refresh token UUID válido")
    void shouldReturnValidUuidRefreshToken() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "uuid@email.com",
                "UUID User",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("uuid@email.com", "senha@123");

        // Act & Assert
        String responseContent = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Validar que o refresh token é um UUID válido
        var response = objectMapper.readTree(responseContent);
        String refreshToken = response.get("refreshToken").asText();
        assertThat(java.util.UUID.fromString(refreshToken)).isNotNull();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 401 quando email não existe")
    void shouldReturn401WhenEmailNotFound() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("naoexiste@email.com", "senha@123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("Email ou senha não conferem")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 401 quando senha incorreta")
    void shouldReturn401WhenPasswordIncorrect() throws Exception {
        // Arrange - Registrar usuário
        RegisterRequest registerRequest = new RegisterRequest(
                "senhaerrada@email.com",
                "Usuario Teste",
                "senhaCorreta@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Tentar login com senha errada
        LoginRequest loginRequest = new LoginRequest("senhaerrada@email.com", "senhaErrada@456");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("Email ou senha não conferem")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 400 quando email inválido")
    void shouldReturn400WhenEmailInvalid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("email-invalido", "senha@123");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field", is("email")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve retornar 400 quando campos são nulos")
    void shouldReturn400WhenFieldsAreNull() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(null, null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve persistir refresh token com hash")
    void shouldPersistRefreshTokenWithHash() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "hash@email.com",
                "Hash User",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("hash@email.com", "senha@123");

        // Act
        String responseContent = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .header("User-Agent", "TestBrowser/1.0"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var response = objectMapper.readTree(responseContent);
        String refreshToken = response.get("refreshToken").asText();

        // Assert - Verificar que o refresh token foi persistido (mas o hash é diferente do token)
        var persistedTokens = refreshTokenRepository.findAll();

        assertThat(persistedTokens).hasSize(1);
        assertThat(persistedTokens.get(0).getTokenHash()).isNotEqualTo(refreshToken); // Hash é diferente
        assertThat(persistedTokens.get(0).getUserAgent()).isNotNull();
        assertThat(persistedTokens.get(0).getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve capturar user-agent e IP corretamente")
    void shouldCaptureUserAgentAndIp() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "metadata@email.com",
                "Metadata User",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("metadata@email.com", "senha@123");

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .header("User-Agent", "TestBrowser/1.0")
                        .header("X-Forwarded-For", "203.0.113.42"))
                .andExpect(status().isOk());

        // Assert
        var persistedTokens = refreshTokenRepository.findAll();
        assertThat(persistedTokens).hasSize(1);
        assertThat(persistedTokens.get(0).getUserAgent()).isEqualTo("TestBrowser/1.0");
        assertThat(persistedTokens.get(0).getIp()).isEqualTo("203.0.113.42");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve permitir múltiplos logins do mesmo usuário")
    void shouldAllowMultipleLoginsFromSameUser() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "multiplos@email.com",
                "Multi User",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("multiplos@email.com", "senha@123");

        // Act - Fazer login 3 vezes
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Assert - Deve ter 3 refresh tokens persistidos
        var persistedTokens = refreshTokenRepository.findAll();
        assertThat(persistedTokens).hasSize(3);
    }
}


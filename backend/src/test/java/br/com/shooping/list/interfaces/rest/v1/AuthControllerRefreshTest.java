package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.auth.LoginRequest;
import br.com.shooping.list.application.dto.auth.RefreshTokenRequest;
import br.com.shooping.list.application.dto.auth.RegisterRequest;
import br.com.shooping.list.domain.user.RefreshToken;
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
 * Testes de integração para endpoint de Refresh Token no AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Refresh Token - Testes de Integração")
class AuthControllerRefreshTest {

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
    @DisplayName("POST /api/v1/auth/refresh - Deve renovar tokens com sucesso")
    void shouldRefreshTokensSuccessfully() throws Exception {
        // Arrange - Registrar e fazer login para obter refresh token
        RegisterRequest registerRequest = new RegisterRequest(
                "refresh@email.com",
                "Usuario Teste",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("refresh@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest))
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", not(refreshToken))) // Novo token diferente
                .andExpect(jsonPath("$.expiresIn", notNullValue()))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve rotacionar refresh token")
    void shouldRotateRefreshToken() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "rotacao@email.com",
                "Usuario Rotacao",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("rotacao@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(oldRefreshToken);

        // Act - Fazer refresh
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken = objectMapper.readTree(refreshResponse).get("refreshToken").asText();

        // Assert - Tokens devem ser diferentes
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        // Assert - Deve ter 2 refresh tokens no banco (antigo revogado + novo válido)
        var tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(2);
        assertThat(tokens.stream().filter(RefreshToken::isRevoked).count()).isEqualTo(1);
        assertThat(tokens.stream().filter(RefreshToken::isValid).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve revogar token antigo após uso")
    void shouldRevokeOldTokenAfterUse() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "revoke@email.com",
                "Usuario Revoke",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("revoke@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act - Usar refresh token
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        // Assert - Tentar reusar o mesmo token deve falhar (401)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("já foi utilizado")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar 401 quando token inválido")
    void shouldReturn401WhenTokenInvalid() throws Exception {
        // Arrange
        RefreshTokenRequest invalidRequest = new RefreshTokenRequest("49a6336d-invalid-token-uuid");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", containsString("inválido")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar 400 quando refresh token vazio")
    void shouldReturn400WhenTokenEmpty() throws Exception {
        // Arrange
        RefreshTokenRequest emptyRequest = new RefreshTokenRequest("");

        // Act & Assert - Com cookies, token vazio no body sem cookie retorna IllegalArgumentException
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("obrigatório")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar 400 quando refresh token null")
    void shouldReturn400WhenTokenNull() throws Exception {
        // Arrange
        RefreshTokenRequest nullRequest = new RefreshTokenRequest(null);

        // Act & Assert - Com cookies, token null no body sem cookie retorna IllegalArgumentException
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("obrigatório")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve vincular token antigo ao novo via replacedBy")
    void shouldLinkOldTokenToNewViaReplacedBy() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "link@email.com",
                "Usuario Link",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("link@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk());

        // Assert
        var tokens = refreshTokenRepository.findAll();
        var revokedToken = tokens.stream().filter(RefreshToken::isRevoked).findFirst().orElseThrow();
        var newToken = tokens.stream().filter(RefreshToken::isValid).findFirst().orElseThrow();

        assertThat(revokedToken.getReplacedByTokenId()).isEqualTo(newToken.getId());
        assertThat(revokedToken.getRevokedAt()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve retornar novo access token JWT válido")
    void shouldReturnValidNewAccessToken() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "jwt@email.com",
                "Usuario JWT",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("jwt@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert - Validar que é um JWT válido
        String newAccessToken = objectMapper.readTree(refreshResponse).get("accessToken").asText();
        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).contains("."); // JWT válido (3 partes)
        assertThat(newAccessToken.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve capturar metadata (user-agent, IP)")
    void shouldCaptureMetadata() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "metadata@email.com",
                "Usuario Metadata",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("metadata@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest))
                        .header("User-Agent", "RefreshBrowser/2.0")
                        .header("X-Forwarded-For", "198.51.100.42"))
                .andExpect(status().isOk());

        // Assert - Novo token deve ter metadata atualizada
        var tokens = refreshTokenRepository.findAll();
        var newToken = tokens.stream()
                .filter(RefreshToken::isValid)
                .findFirst()
                .orElseThrow();

        assertThat(newToken.getUserAgent()).isEqualTo("RefreshBrowser/2.0");
        assertThat(newToken.getIp()).isEqualTo("198.51.100.42");
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Deve permitir múltiplos refreshes sequenciais")
    void shouldAllowMultipleSequentialRefreshes() throws Exception {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(
                "multiple@email.com",
                "Usuario Multiple",
                "senha@123"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest("multiple@email.com", "senha@123");
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String currentRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // Act - Fazer 3 refreshes sequenciais
        for (int i = 0; i < 3; i++) {
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest(currentRefreshToken);

            String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            currentRefreshToken = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        }

        // Assert - Deve ter 4 tokens no total (1 do login + 3 dos refreshes)
        var tokens = refreshTokenRepository.findAll();
        assertThat(tokens).hasSize(4);
        assertThat(tokens.stream().filter(RefreshToken::isRevoked).count()).isEqualTo(3);
        assertThat(tokens.stream().filter(RefreshToken::isValid).count()).isEqualTo(1);
    }
}


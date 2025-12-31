package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.UserRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validar JWT Authentication Filter
 * e endpoint protegido /api/v1/users/me
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JWT Authentication Filter - Testes de Integração")
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestDataSetup testDataSetup;

    @BeforeEach
    void setUp() {
        // Ordem importante: deletar refresh tokens primeiro (FK constraint)
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        testDataSetup.createDefaultRoles();
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar dados do usuário com JWT válido")
    void shouldReturnUserDataWithValidJwt() throws Exception {
        // Arrange - Registrar e fazer login
        String registerJson = """
                {
                    "email": "jwt-test@email.com",
                    "name": "JWT Test User",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String loginJson = """
                {
                    "email": "jwt-test@email.com",
                    "password": "senha@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        // Act & Assert - Usar JWT para acessar endpoint protegido
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email", is("jwt-test@email.com")))
                .andExpect(jsonPath("$.name", is("JWT Test User")))
                .andExpect(jsonPath("$.provider", is("LOCAL")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar 401 sem token JWT")
    void shouldReturn401WithoutJwt() throws Exception {
        // Act & Assert - Tentar acessar sem token
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar 401 com token inválido")
    void shouldReturn401WithInvalidJwt() throws Exception {
        // Arrange - Token inválido (formato correto mas assinatura inválida)
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjk5OTk5OTk5OTl9.invalid-signature";

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar 401 com token expirado")
    void shouldReturn401WithExpiredJwt() throws Exception {
        // Arrange - Registrar usuário
        String registerJson = """
                {
                    "email": "expired-test@email.com",
                    "name": "Expired Test",
                    "password": "senha@123"
                }
                """;

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(registerResponseBody).get("id").asLong();

        // Criar token expirado manualmente (expira em 1 segundo)
        String expiredToken = jwtService.generateAccessTokenWithCustomExpiration(
                userId,
                "expired-test@email.com",
                "Expired Test",
                "LOCAL",
                1000L // 1 segundo
        );

        // Aguardar expiração
        Thread.sleep(2000);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar 401 com Bearer token malformado")
    void shouldReturn401WithMalformedBearerToken() throws Exception {
        // Act & Assert - Token sem Bearer prefix
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "not-a-bearer-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve retornar 401 com Bearer vazio")
    void shouldReturn401WithEmptyBearer() throws Exception {
        // Act & Assert - Bearer sem token
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Deve continuar funcionando (rotas públicas não afetadas)")
    void shouldAllowPublicRoutesWithoutJwt() throws Exception {
        // Arrange - Registrar usuário
        String registerJson = """
                {
                    "email": "public-test@email.com",
                    "name": "Public Test",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String loginJson = """
                {
                    "email": "public-test@email.com",
                    "password": "senha@123"
                }
                """;

        // Act & Assert - Login deve funcionar sem JWT (rota pública)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - Deve validar que userId extraído do JWT corresponde ao usuário correto")
    void shouldExtractCorrectUserIdFromJwt() throws Exception {
        // Arrange - Registrar dois usuários
        String user1Json = """
                {
                    "email": "user1@email.com",
                    "name": "User One",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user1Json));

        String user2Json = """
                {
                    "email": "user2@email.com",
                    "name": "User Two",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user2Json));

        // Login com user2
        String loginJson = """
                {
                    "email": "user2@email.com",
                    "password": "senha@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        // Act & Assert - Deve retornar dados do user2, não do user1
        MvcResult meResult = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user2@email.com")))
                .andExpect(jsonPath("$.name", is("User Two")))
                .andReturn();

        String meResponseBody = meResult.getResponse().getContentAsString();
        Long returnedUserId = objectMapper.readTree(meResponseBody).get("id").asLong();

        // Validar que é realmente o user2
        assertThat(returnedUserId).isNotNull();
        assertThat(returnedUserId).isGreaterThan(1L); // user2 tem id maior que user1
    }
}


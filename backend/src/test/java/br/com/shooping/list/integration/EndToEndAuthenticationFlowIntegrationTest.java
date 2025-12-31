package br.com.shooping.list.integration;

import br.com.shooping.list.AbstractIntegrationTest;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator.GoogleUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração end-to-end completos com Testcontainers MySQL.
 * <p>
 * Valida os fluxos principais da aplicação:
 * 1. Register → Login → Acesso a endpoint protegido
 * 2. Refresh token com rotação
 * 3. Logout revoga refresh token
 * 4. Google OAuth2 login
 * <p>
 * Usa banco MySQL real via Testcontainers para garantir
 * que os testes reflitam o comportamento de produção.
 * <p>
 * <p>
 * ⚠️ **ATENÇÃO:** Estes testes requerem Docker rodando!
 * <p>
 * Para executar:
 * 1. Inicie o Docker: `docker ps` deve funcionar
 * 2. Remova @Disabled ou execute: `./mvnw test -Dtest=EndToEndAuthenticationFlowIntegrationTest`
 * <p>
 * Testes padrão usam H2 (não requerem Docker).
 */
@Disabled("Requer Docker rodando. Execute manualmente: ./mvnw test -Dtest=EndToEndAuthenticationFlowIntegrationTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("End-to-End Integration Tests - Fluxo Completo de Autenticação")
class EndToEndAuthenticationFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private GoogleTokenValidator googleTokenValidator;

    private MockMvc mockMvc;

    // Dados de teste compartilhados entre os testes
    private static final String TEST_EMAIL = "integration@test.com";
    private static final String TEST_PASSWORD = "Test@123456";
    private static final String TEST_NAME = "Integration Test User";

    private static String accessToken;
    private static String refreshToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpa dados entre testes para garantir isolamento
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Fluxo Completo: Register → Login → Acesso Endpoint Protegido")
    void shouldCompleteFullAuthenticationFlow() throws Exception {
        // ========== PASSO 1: REGISTER ==========
        String registerPayload = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "name", TEST_NAME,
                "password", TEST_PASSWORD
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.provider").value("LOCAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists());

        // Valida que usuário foi criado no banco
        assertThat(userRepository.existsByEmail(TEST_EMAIL)).isTrue();

        // ========== PASSO 2: LOGIN ==========
        String loginPayload = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(header().exists("Set-Cookie"))
                .andReturn();

        // Extrai tokens da resposta
        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginResponse = objectMapper.readValue(responseBody, Map.class);
        accessToken = (String) loginResponse.get("accessToken");
        refreshToken = (String) loginResponse.get("refreshToken");

        assertThat(accessToken).isNotNull().isNotEmpty();
        assertThat(refreshToken).isNotNull().matches("^[0-9a-f-]{36}$"); // UUID format

        // Valida que refresh token foi persistido (como hash)
        long refreshTokenCount = refreshTokenRepository.findAll().stream().count();
        assertThat(refreshTokenCount).isEqualTo(1);

        // ========== PASSO 3: ACESSO A ENDPOINT PROTEGIDO ==========
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.provider").value("LOCAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @Order(2)
    @DisplayName("Fluxo de Refresh Token: Login → Refresh (Rotação) → Novo Access Token")
    void shouldRefreshTokenWithRotation() throws Exception {
        // ========== SETUP: Registra e faz login ==========
        registerAndLogin();

        // ========== PASSO 1: REFRESH TOKEN ==========
        String refreshPayload = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken
        ));

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

        // Extrai novos tokens
        String responseBody = refreshResult.getResponse().getContentAsString();
        Map<String, Object> refreshResponse = objectMapper.readValue(responseBody, Map.class);
        String newAccessToken = (String) refreshResponse.get("accessToken");
        String newRefreshToken = (String) refreshResponse.get("refreshToken");

        // Valida que tokens são diferentes (rotação funcionou)
        assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken);
        assertThat(newRefreshToken).isNotNull().isNotEqualTo(refreshToken);

        // ========== PASSO 2: TENTATIVA DE REUSO DO TOKEN ANTIGO (DEVE FALHAR) ==========
        String oldRefreshPayload = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken // Token antigo revogado
        ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oldRefreshPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(containsString("já foi utilizado")));

        // ========== PASSO 3: NOVO ACCESS TOKEN FUNCIONA ==========
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @Order(3)
    @DisplayName("Fluxo de Logout: Login → Logout → Refresh Token Revogado")
    void shouldLogoutAndRevokeRefreshToken() throws Exception {
        // ========== SETUP: Registra e faz login ==========
        registerAndLogin();

        // ========== PASSO 1: LOGOUT ==========
        String logoutPayload = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken
        ));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutPayload))
                .andExpect(status().isNoContent());

        // ========== PASSO 2: TENTATIVA DE REFRESH APÓS LOGOUT (DEVE FALHAR) ==========
        String refreshPayload = objectMapper.writeValueAsString(Map.of(
                "refreshToken", refreshToken
        ));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(containsString("revogado")));
    }

    @Test
    @Order(4)
    @DisplayName("Fluxo Google OAuth2: Google Login → Acesso Endpoint Protegido")
    void shouldLoginWithGoogleAndAccessProtectedEndpoint() throws Exception {
        // ========== SETUP: Mock do Google Token Validator ==========
        String googleIdToken = "mock.google.id.token";
        String googleEmail = "google@test.com";
        String googleName = "Google Test User";
        String googleId = "google-123456";

        GoogleUserInfo googleUserInfo = new GoogleUserInfo(
                googleEmail,
                googleName,
                googleId,
                true // email verified
        );

        when(googleTokenValidator.validate(anyString())).thenReturn(googleUserInfo);

        // ========== PASSO 1: GOOGLE LOGIN ==========
        String googleLoginPayload = objectMapper.writeValueAsString(Map.of(
                "idToken", googleIdToken
        ));

        MvcResult googleLoginResult = mockMvc.perform(post("/api/v1/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleLoginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

        // Extrai access token
        String responseBody = googleLoginResult.getResponse().getContentAsString();
        Map<String, Object> googleLoginResponse = objectMapper.readValue(responseBody, Map.class);
        String googleAccessToken = (String) googleLoginResponse.get("accessToken");

        // Valida que usuário Google foi provisionado
        assertThat(userRepository.existsByEmail(googleEmail)).isTrue();

        // ========== PASSO 2: ACESSO A ENDPOINT PROTEGIDO COM TOKEN GOOGLE ==========
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + googleAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(googleEmail))
                .andExpect(jsonPath("$.name").value(googleName))
                .andExpect(jsonPath("$.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @Order(5)
    @DisplayName("Fluxo RBAC: Usuário sem role ADMIN não acessa endpoint administrativo")
    void shouldDenyAccessToAdminEndpointForRegularUser() throws Exception {
        // ========== SETUP: Registra e faz login ==========
        registerAndLogin();

        // ========== TENTATIVA DE ACESSO A ENDPOINT ADMIN (DEVE FALHAR) ==========
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(containsString("permissão")));
    }

    @Test
    @Order(6)
    @DisplayName("Fluxo com Correlation ID: Request com correlation-id retorna mesmo ID")
    void shouldPreserveCorrelationId() throws Exception {
        // ========== REQUEST COM CORRELATION ID CUSTOMIZADO ==========
        String customCorrelationId = "test-correlation-123";

        MvcResult result = mockMvc.perform(get("/api/v1/health")
                        .header("X-Correlation-Id", customCorrelationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", customCorrelationId))
                .andReturn();

        // ========== ERRO TAMBÉM DEVE INCLUIR CORRELATION ID ==========
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Correlation-Id", customCorrelationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // payload inválido
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", customCorrelationId))
                .andExpect(jsonPath("$.correlationId").value(customCorrelationId));
    }

    /**
     * Helper method: Registra usuário e faz login, armazenando tokens.
     */
    private void registerAndLogin() throws Exception {
        // Register
        String registerPayload = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "name", TEST_NAME,
                "password", TEST_PASSWORD
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        // Login
        String loginPayload = objectMapper.writeValueAsString(Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginResponse = objectMapper.readValue(responseBody, Map.class);
        accessToken = (String) loginResponse.get("accessToken");
        refreshToken = (String) loginResponse.get("refreshToken");
    }
}


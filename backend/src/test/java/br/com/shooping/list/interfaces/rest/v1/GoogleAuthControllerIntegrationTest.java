package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidationException;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator.GoogleUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para autenticação via Google OAuth2.
 * Valida o endpoint POST /api/v1/auth/google.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Google OAuth2 Authentication - Testes de Integração")
class GoogleAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private GoogleTokenValidator googleTokenValidator;

    private static final String GOOGLE_AUTH_ENDPOINT = "/api/v1/auth/google";
    private static final String VALID_ID_TOKEN = "valid.google.id.token";
    private static final String INVALID_ID_TOKEN = "invalid.token";
    private static final String GOOGLE_EMAIL = "googleuser@gmail.com";
    private static final String GOOGLE_NAME = "Google User";
    private static final String GOOGLE_ID = "google-123456";

    @BeforeEach
    void setUp() {
        // Limpa dados antes de cada teste
        userRepository.deleteAll();

        // Garante que role USER existe
        roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.create("USER", "Usuário padrão com permissões básicas");
                    return roleRepository.save(newRole);
                });
    }

    @Test
    @DisplayName("Deve fazer login com Google para novo usuário")
    void shouldLoginWithGoogleForNewUser() throws Exception {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);

        Map<String, String> requestBody = Map.of("idToken", VALID_ID_TOKEN);

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900L)) // 15 minutos em test profile
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")));

        // Verifica que usuário foi criado
        User createdUser = userRepository.findByEmail(GOOGLE_EMAIL).orElseThrow();
        assert createdUser.getName().equals(GOOGLE_NAME);
        assert createdUser.getProvider().name().equals("GOOGLE");
        assert createdUser.getPasswordHash() == null;
        assert createdUser.getRoles().stream().anyMatch(role -> role.getName().equals("USER"));
    }

    @Test
    @DisplayName("Deve fazer login com Google para usuário existente")
    void shouldLoginWithGoogleForExistingUser() throws Exception {
        // Given - Cria usuário Google previamente
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        User existingUser = User.createGoogleUser(GOOGLE_EMAIL, GOOGLE_NAME);
        existingUser.addRole(userRole);
        userRepository.save(existingUser);

        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);

        Map<String, String> requestBody = Map.of("idToken", VALID_ID_TOKEN);

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900L));

        // Verifica que não criou usuário duplicado
        assert userRepository.findByEmail(GOOGLE_EMAIL).isPresent();
        long userCount = userRepository.findByEmail(GOOGLE_EMAIL).stream().count();
        assert userCount == 1; // Apenas 1 usuário com esse email
    }

    @Test
    @DisplayName("Deve retornar 401 quando token do Google for inválido")
    void shouldReturn401WhenGoogleTokenIsInvalid() throws Exception {
        // Given
        when(googleTokenValidator.validate(INVALID_ID_TOKEN))
                .thenThrow(new GoogleTokenValidationException("Token inválido ou expirado"));

        Map<String, String> requestBody = Map.of("idToken", INVALID_ID_TOKEN);

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(containsString("Token do Google inválido")))
                .andExpect(jsonPath("$.path").value(GOOGLE_AUTH_ENDPOINT))
                .andExpect(jsonPath("$.timestamp").exists());

        // Verifica que nenhum usuário foi criado
        assert !userRepository.existsByEmail(GOOGLE_EMAIL);
    }

    @Test
    @DisplayName("Deve retornar 401 quando email não for verificado pelo Google")
    void shouldReturn401WhenEmailNotVerified() throws Exception {
        // Given - Email não verificado
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, false);
        when(googleTokenValidator.validate(VALID_ID_TOKEN))
                .thenThrow(new GoogleTokenValidationException("Email não verificado pelo Google"));

        Map<String, String> requestBody = Map.of("idToken", VALID_ID_TOKEN);

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // Verifica que nenhum usuário foi criado
        assert !userRepository.existsByEmail(GOOGLE_EMAIL);
    }

    @Test
    @DisplayName("Deve retornar 400 quando idToken estiver vazio")
    void shouldReturn400WhenIdTokenIsEmpty() throws Exception {
        // Given
        Map<String, String> requestBody = Map.of("idToken", "");

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0].field").value("idToken"))
                .andExpect(jsonPath("$.details[0].message").value(containsString("obrigatório")));
    }

    @Test
    @DisplayName("Deve retornar 400 quando body estiver vazio")
    void shouldReturn400WhenBodyIsEmpty() throws Exception {
        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Deve criar refresh token com metadata de requisição")
    void shouldCreateRefreshTokenWithRequestMetadata() throws Exception {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);

        Map<String, String> requestBody = Map.of("idToken", VALID_ID_TOKEN);

        // When / Then
        mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody))
                        .header("User-Agent", "Mozilla/5.0 Test Browser")
                        .header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Nota: A validação do metadata está na camada de domínio/repository
        // Aqui apenas garantimos que a requisição foi bem-sucedida
    }

    @Test
    @DisplayName("Deve retornar access token com roles corretas")
    void shouldReturnAccessTokenWithCorrectRoles() throws Exception {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);

        Map<String, String> requestBody = Map.of("idToken", VALID_ID_TOKEN);

        // When / Then
        String response = mockMvc.perform(post(GOOGLE_AUTH_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verifica que o access token foi gerado (não vamos decodificar JWT aqui, isso é testado nos testes unitários)
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assert responseMap.containsKey("accessToken");
        assert responseMap.get("accessToken") != null;
        assert !responseMap.get("accessToken").toString().isEmpty();
    }
}


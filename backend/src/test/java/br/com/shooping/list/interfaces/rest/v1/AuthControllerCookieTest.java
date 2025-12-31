package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.application.dto.auth.LoginRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validar cookies HttpOnly nos endpoints de autenticação
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController - Cookies - Testes de Integração")
class AuthControllerCookieTest {

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
        // Ordem importante: deletar refresh tokens primeiro (FK constraint)
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Criar roles padrão (USER e ADMIN) no banco H2
        testDataSetup.createDefaultRoles();
    }

    @Test
    @DisplayName("Login deve adicionar cookie de refresh token")
    void loginShouldAddRefreshTokenCookie() throws Exception {
        // Arrange - Registrar usuário
        String registerJson = """
                {
                    "email": "cookie@test.com",
                    "name": "Cookie Test",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Arrange - Preparar login
        String loginJson = """
                {
                    "email": "cookie@test.com",
                    "password": "senha@123"
                }
                """;

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists()) // Cookie-only=false em test
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/api/v1/auth"))
                .andReturn();

        // Validar que o cookie foi criado
        Cookie refreshCookie = result.getResponse().getCookie("refreshToken");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isNotEmpty();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(86400); // 1 dia no test
    }

    @Test
    @DisplayName("Refresh deve aceitar token do cookie e adicionar novo cookie")
    void refreshShouldAcceptCookieAndSetNewCookie() throws Exception {
        // Arrange - Registrar e fazer login
        String registerJson = """
                {
                    "email": "refresh-cookie@test.com",
                    "name": "Refresh Cookie",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String loginJson = """
                {
                    "email": "refresh-cookie@test.com",
                    "password": "senha@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        Cookie oldRefreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // Act - Refresh usando cookie
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(oldRefreshCookie) // Envia cookie do login
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vazio, usa cookie
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        // Assert - Novo cookie foi criado
        Cookie newRefreshCookie = refreshResult.getResponse().getCookie("refreshToken");
        assertThat(newRefreshCookie).isNotNull();
        assertThat(newRefreshCookie.getValue()).isNotEmpty();
        assertThat(newRefreshCookie.getValue()).isNotEqualTo(oldRefreshCookie.getValue()); // Rotação
        assertThat(newRefreshCookie.isHttpOnly()).isTrue();
    }

    @Test
    @DisplayName("Logout deve remover cookie (Max-Age=0)")
    void logoutShouldClearCookie() throws Exception {
        // Arrange - Registrar e fazer login
        String registerJson = """
                {
                    "email": "logout-cookie@test.com",
                    "name": "Logout Cookie",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String loginJson = """
                {
                    "email": "logout-cookie@test.com",
                    "password": "senha@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

        // Act - Logout usando cookie
        MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie) // Envia cookie do login
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Body vazio, usa cookie
                .andExpect(status().isNoContent())
                .andReturn();

        // Assert - Cookie foi removido (Max-Age=0)
        Cookie clearedCookie = logoutResult.getResponse().getCookie("refreshToken");
        assertThat(clearedCookie).isNotNull();
        assertThat(clearedCookie.getMaxAge()).isEqualTo(0); // Remove imediatamente
    }

    @Test
    @DisplayName("Refresh deve aceitar token do body quando cookie não está presente (backward compatibility)")
    void refreshShouldAcceptBodyWhenNoCookie() throws Exception {
        // Arrange - Registrar e fazer login
        String registerJson = """
                {
                    "email": "body-token@test.com",
                    "name": "Body Token",
                    "password": "senha@123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String loginJson = """
                {
                    "email": "body-token@test.com",
                    "password": "senha@123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseBody).get("refreshToken").asText();

        // Act - Refresh usando body (sem cookie)
        String refreshJson = String.format("""
                {
                    "refreshToken": "%s"
                }
                """, refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson)) // Envia no body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken")); // Cookie ainda é criado
    }

    @Test
    @DisplayName("Refresh deve retornar 400 quando nem cookie nem body estão presentes")
    void refreshShouldReturn400WhenNoCookieAndNoBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Sem token no body e sem cookie
                .andExpect(status().isBadRequest());
    }
}


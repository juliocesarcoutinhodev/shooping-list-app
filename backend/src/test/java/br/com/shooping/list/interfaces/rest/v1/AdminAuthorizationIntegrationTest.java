package br.com.shooping.list.interfaces.rest.v1;

import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validar autorização baseada em roles (RBAC).
 * Valida que:
 * - Usuários com role USER não podem acessar endpoints administrativos (403)
 * - Usuários com role ADMIN podem acessar endpoints administrativos (200)
 * - Usuários sem autenticação não podem acessar endpoints administrativos (401)
 * - AccessDeniedHandler retorna formato de erro padronizado
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Authorization - Testes de Integração RBAC")
class AdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userWithUserRole;
    private User userWithAdminRole;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Limpar base
        userRepository.deleteAll();

        // Garantir que roles existem (cria se não existir)
        Role roleUser = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.create("USER", "Usuário padrão com permissões básicas");
                    return roleRepository.save(newRole);
                });

        Role roleAdmin = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = Role.create("ADMIN", "Administrador com permissões completas");
                    return roleRepository.save(newRole);
                });

        // Criar usuário com role USER
        userWithUserRole = User.createLocalUser(
                "user@test.com",
                "Regular User",
                passwordEncoder.encode("senha123")
        );
        userWithUserRole.addRole(roleUser);
        userWithUserRole = userRepository.save(userWithUserRole);

        // Criar usuário com role ADMIN
        userWithAdminRole = User.createLocalUser(
                "admin@test.com",
                "Admin User",
                passwordEncoder.encode("senha123")
        );
        userWithAdminRole.addRole(roleAdmin);
        userWithAdminRole = userRepository.save(userWithAdminRole);

        // Gerar tokens JWT
        userToken = jwtService.generateAccessToken(userWithUserRole);
        adminToken = jwtService.generateAccessToken(userWithAdminRole);
    }

    @Test
    @DisplayName("Deve retornar 403 quando USER tenta acessar endpoint ADMIN")
    void shouldReturn403WhenUserTriesToAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Acesso negado. Você não possui permissões necessárias para acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/ping"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 200 quando ADMIN acessa endpoint ADMIN")
    void shouldReturn200WhenAdminAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("pong"))
                .andExpect(jsonPath("$.userId").value(userWithAdminRole.getId().toString()))
                .andExpect(jsonPath("$.authorities").isArray())
                .andExpect(jsonPath("$.authorities", hasItem("ROLE_ADMIN")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando tenta acessar endpoint ADMIN sem token")
    void shouldReturn401WhenAccessingAdminEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Autenticação requerida. Por favor, forneça um token JWT válido."))
                .andExpect(jsonPath("$.path").value("/api/v1/admin/ping"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Deve retornar 401 quando tenta acessar endpoint ADMIN com token inválido")
    void shouldReturn401WhenAccessingAdminEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Deve retornar 403 para múltiplas tentativas de USER em endpoint ADMIN")
    void shouldReturn403ForMultipleUserAttemptsOnAdminEndpoint() throws Exception {
        // Primeira tentativa
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Segunda tentativa
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Terceira tentativa
        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Deve permitir USER acessar endpoints não administrativos")
    void shouldAllowUserToAccessNonAdminEndpoints() throws Exception {
        // Endpoint /users/me deve funcionar para USER
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userWithUserRole.getId()))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @DisplayName("Deve permitir ADMIN acessar endpoints não administrativos")
    void shouldAllowAdminToAccessNonAdminEndpoints() throws Exception {
        // Endpoint /users/me deve funcionar para ADMIN também
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userWithAdminRole.getId()))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }
}


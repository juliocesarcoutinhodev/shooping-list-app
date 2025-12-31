package br.com.shooping.list.infrastructure.security;

import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.infrastructure.exception.ExpiredJwtException;
import br.com.shooping.list.infrastructure.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;

/**
 * Testes unitários para JwtService
 * <p>
 * Valida:
 * - Geração de tokens JWT válidos
 * - Extração de claims (userId, email, name)
 * - Validação de tokens (válidos, expirados, inválidos)
 * - Tratamento de exceções customizadas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Testes Unitários")
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private String testSecret;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        // Secret com mínimo 256 bits para HS256
        testSecret = "test-secret-key-minimum-256-bits-for-hs256-algorithm-secure";
        secretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        // Mock das propriedades JWT (usando lenient para testes que não usam todos os mocks)
        lenient().when(jwtProperties.getSecret()).thenReturn(testSecret);
        lenient().when(jwtProperties.getIssuer()).thenReturn("shopping-list-test");

        JwtProperties.AccessToken accessToken = new JwtProperties.AccessToken();
        accessToken.setExpiration(Duration.ofHours(1));
        lenient().when(jwtProperties.getAccessToken()).thenReturn(accessToken);

        // Criar usuário de teste
        testUser = User.createLocalUser(
                "test@example.com",
                "Test User",
                "$2a$10$hashedPassword"
        );
        // Simular ID (normalmente seria setado pelo JPA)
        setUserId(testUser, 1L);
    }

    @Test
    @DisplayName("Deve gerar access token válido com todos os claims")
    void shouldGenerateValidAccessToken() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull().isNotEmpty();

        // Validar estrutura JWT (header.payload.signature)
        assertThat(token.split("\\.")).hasSize(3);

        // Extrair e validar claims
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Test User");
        assertThat(claims.get("provider", String.class)).isEqualTo("LOCAL");
        assertThat(claims.getIssuer()).isEqualTo("shopping-list-test");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("Deve extrair userId corretamente do token")
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String userId = jwtService.extractUserId(token);

        // Then
        assertThat(userId).isEqualTo("1");
    }

    @Test
    @DisplayName("Deve extrair email corretamente do token")
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String email = jwtService.extractEmail(token);

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Deve extrair nome corretamente do token")
    void shouldExtractNameFromToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String name = jwtService.extractName(token);

        // Then
        assertThat(name).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Deve validar token válido sem lançar exceção")
    void shouldValidateValidToken() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When & Then
        assertThatCode(() -> jwtService.validateToken(token))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar ExpiredJwtException para token expirado")
    void shouldThrowExpiredJwtExceptionForExpiredToken() {
        // Given - Token expirado (criado no passado)
        Instant past = Instant.now().minus(Duration.ofHours(2));
        Instant expiration = past.plus(Duration.ofMinutes(1)); // Expirou há 1h59min

        String expiredToken = Jwts.builder()
                .subject("1")
                .issuer("shopping-list-test")
                .issuedAt(Date.from(past))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("Token JWT expirado");
    }

    @Test
    @DisplayName("Deve lançar InvalidJwtException para assinatura inválida")
    void shouldThrowInvalidJwtExceptionForInvalidSignature() {
        // Given - Token com assinatura diferente
        String wrongSecret = "wrong-secret-key-minimum-256-bits-for-hs256-algorithm-wrong";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithWrongSignature = Jwts.builder()
                .subject("1")
                .issuer("shopping-list-test")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
                .signWith(wrongKey)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(tokenWithWrongSignature))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Assinatura do token JWT inválida");
    }

    @Test
    @DisplayName("Deve lançar InvalidJwtException para token malformado")
    void shouldThrowInvalidJwtExceptionForMalformedToken() {
        // Given
        String malformedToken = "invalid.token.structure";

        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(malformedToken))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Token JWT malformado");
    }

    @Test
    @DisplayName("Deve lançar InvalidJwtException para token vazio")
    void shouldThrowInvalidJwtExceptionForEmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(emptyToken))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Erro ao validar token JWT");
    }

    @Test
    @DisplayName("Deve lançar InvalidJwtException para token null")
    void shouldThrowInvalidJwtExceptionForNullToken() {
        // When & Then
        assertThatThrownBy(() -> jwtService.validateToken(null))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessageContaining("Erro ao validar token JWT");
    }

    @Test
    @DisplayName("Deve extrair todos os claims corretamente")
    void shouldExtractAllClaims() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Claims claims = jwtService.extractAllClaims(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("email")).isEqualTo("test@example.com");
        assertThat(claims.get("name")).isEqualTo("Test User");
        assertThat(claims.get("provider")).isEqualTo("LOCAL");
        assertThat(claims.getIssuer()).isEqualTo("shopping-list-test");
        assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Deve gerar token para usuário Google sem password hash")
    void shouldGenerateTokenForGoogleUser() {
        // Given
        User googleUser = User.createGoogleUser("google@example.com", "Google User");
        setUserId(googleUser, 2L);

        // When
        String token = jwtService.generateAccessToken(googleUser);

        // Then
        assertThat(token).isNotNull().isNotEmpty();

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.getSubject()).isEqualTo("2");
        assertThat(claims.get("email")).isEqualTo("google@example.com");
        assertThat(claims.get("provider")).isEqualTo("GOOGLE");
    }

    @Test
    @DisplayName("Token deve expirar após o tempo configurado")
    void shouldExpireAfterConfiguredTime() {
        // Given
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.extractAllClaims(token);

        // Then
        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiresAt = claims.getExpiration().toInstant();
        Duration tokenDuration = Duration.between(issuedAt, expiresAt);

        // Deve expirar em aproximadamente 1 hora (configurado no setUp)
        assertThat(tokenDuration.toMinutes())
                .isGreaterThanOrEqualTo(59)
                .isLessThanOrEqualTo(61);
    }

    @Test
    @DisplayName("Deve incluir roles no token e extrair corretamente")
    void shouldIncludeRolesInTokenAndExtractCorrectly() {
        // Given - Adicionar roles ao usuário de teste
        var userRole = createRole(1L, "USER", "Usuário comum");
        addRoleToUser(testUser, userRole);

        // When
        String token = jwtService.generateAccessToken(testUser);
        var extractedRoles = jwtService.extractRoles(token);

        // Then
        assertThat(extractedRoles)
                .isNotNull()
                .hasSize(1)
                .contains("USER");
    }

    @Test
    @DisplayName("Deve incluir múltiplas roles no token")
    void shouldIncludeMultipleRolesInToken() {
        // Given - Adicionar múltiplas roles ao usuário
        var userRole = createRole(1L, "USER", "Usuário comum");
        var adminRole = createRole(2L, "ADMIN", "Administrador");
        addRoleToUser(testUser, userRole);
        addRoleToUser(testUser, adminRole);

        // When
        String token = jwtService.generateAccessToken(testUser);
        var extractedRoles = jwtService.extractRoles(token);

        // Then
        assertThat(extractedRoles)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem roles")
    void shouldReturnEmptyListWhenUserHasNoRoles() {
        // Given - Usuário sem roles

        // When
        String token = jwtService.generateAccessToken(testUser);
        var extractedRoles = jwtService.extractRoles(token);

        // Then
        assertThat(extractedRoles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Deve validar que claim roles existe no token")
    void shouldValidateRolesClaimExistsInToken() {
        // Given
        var userRole = createRole(1L, "USER", "Usuário comum");
        addRoleToUser(testUser, userRole);

        // When
        String token = jwtService.generateAccessToken(testUser);
        Claims claims = jwtService.extractAllClaims(token);

        // Then
        assertThat(claims.get("roles")).isNotNull();
        assertThat(claims.get("roles")).isInstanceOf(java.util.List.class);
    }

    /**
     * Método auxiliar para setar ID via reflection (simulando comportamento do JPA)
     */
    private void setUserId(User user, Long id) {
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao setar ID do usuário", e);
        }
    }

    /**
     * Método auxiliar para criar role via reflection
     */
    private br.com.shooping.list.domain.user.Role createRole(Long id, String name, String description) {
        try {
            var role = br.com.shooping.list.domain.user.Role.create(name, description);
            var idField = br.com.shooping.list.domain.user.Role.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(role, id);
            return role;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar role", e);
        }
    }

    /**
     * Método auxiliar para adicionar role ao usuário via reflection
     */
    private void addRoleToUser(User user, br.com.shooping.list.domain.user.Role role) {
        user.getRoles().add(role);
    }
}


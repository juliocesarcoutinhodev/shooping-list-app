package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.RefreshTokenRequest;
import br.com.shooping.list.application.dto.auth.RefreshTokenResponse;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.infrastructure.exception.InvalidRefreshTokenException;
import br.com.shooping.list.infrastructure.security.JwtProperties;
import br.com.shooping.list.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RefreshTokenUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase - Testes Unitários")
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    private RefreshTokenRequest validRequest;
    private User validUser;
    private RefreshToken validRefreshToken;
    private JwtProperties.AccessToken accessTokenConfig;
    private JwtProperties.RefreshToken refreshTokenConfig;

    @BeforeEach
    void setUp() throws Exception {
        validRequest = new RefreshTokenRequest("49a6336d-5649-466a-afeb-beee6b2f31d0");

        validUser = User.createLocalUser(
                "usuario@email.com",
                "João Silva",
                "$2a$10$hashedPassword"
        );

        // Simular ID do usuário
        var userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(validUser, 1L);

        // Criar refresh token válido
        validRefreshToken = RefreshToken.create(
                validUser,
                "hashDoTokenOriginal",
                Instant.now().plusSeconds(604800), // 7 dias
                "Mozilla/5.0",
                "192.168.1.1"
        );

        // Simular ID do refresh token
        var tokenIdField = RefreshToken.class.getDeclaredField("id");
        tokenIdField.setAccessible(true);
        tokenIdField.set(validRefreshToken, 1L);

        // Configurar mocks de propriedades JWT
        accessTokenConfig = new JwtProperties.AccessToken();
        accessTokenConfig.setExpiration(Duration.ofMinutes(15));

        refreshTokenConfig = new JwtProperties.RefreshToken();
        refreshTokenConfig.setExpiration(Duration.ofDays(7));
    }

    @Test
    @DisplayName("Deve renovar tokens com sucesso quando refresh token válido")
    void shouldRefreshSuccessfully() {
        // Arrange
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(validUser)).thenReturn("new.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken token = inv.getArgument(0);
            // Simular ID do novo token
            try {
                var idField = RefreshToken.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(token, 2L);
            } catch (Exception e) {
                // ignore
            }
            return token;
        });

        // Act
        RefreshTokenResponse response = refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new.access.token");
        assertThat(response.refreshToken()).isNotNull().isNotEmpty();
        assertThat(response.expiresIn()).isEqualTo(900L); // 15 minutos

        // Verify - Deve ter revogado o token antigo
        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.getReplacedByTokenId()).isNotNull();

        // Verify - Deve ter salvo 2 vezes: novo token + token revogado
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve rotacionar refresh token gerando novo UUID")
    void shouldRotateRefreshToken() {
        // Arrange
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RefreshTokenResponse response = refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert - Novo refresh token deve ser UUID válido e diferente do antigo
        assertThat(response.refreshToken()).isNotEqualTo(validRequest.refreshToken());
        assertThatNoException().isThrownBy(() -> java.util.UUID.fromString(response.refreshToken()));
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("inválido");

        // Verify - Não deve ter gerado novos tokens
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token expirado")
    void shouldThrowExceptionWhenTokenExpired() throws Exception {
        // Arrange - Criar token válido e depois modificar para expirado
        RefreshToken expiredToken = RefreshToken.create(
                validUser,
                "hashDoTokenExpirado",
                Instant.now().plusSeconds(3600), // Criar válido primeiro
                "Mozilla/5.0",
                "192.168.1.1"
        );

        // Modificar para expirado usando reflection
        var expiresAtField = RefreshToken.class.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(expiredToken, Instant.now().minusSeconds(3600)); // Expirou há 1 hora

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expirado");

        // Verify
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token já foi revogado (REUSO)")
    void shouldThrowExceptionWhenTokenAlreadyRevoked() throws Exception {
        // Arrange - Revogar o token
        validRefreshToken.revoke(999L);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("já foi utilizado");

        // Verify - Não deve gerar novos tokens
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("Deve persistir novo refresh token com hash SHA-256")
    void shouldPersistNewTokenWithHash() {
        // Arrange
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RefreshTokenResponse response = refreshTokenUseCase.execute(validRequest, "TestBrowser/1.0", "203.0.113.42");

        // Assert - Capturar o novo token salvo
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(tokenCaptor.capture());

        RefreshToken newToken = tokenCaptor.getAllValues().get(0); // Primeiro save é o novo token
        assertThat(newToken.getTokenHash()).isNotEqualTo(response.refreshToken()); // Hash é diferente
        assertThat(newToken.getUserAgent()).isEqualTo("TestBrowser/1.0");
        assertThat(newToken.getIp()).isEqualTo("203.0.113.42");
        assertThat(newToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Deve vincular token antigo ao novo via replacedByTokenId")
    void shouldLinkOldTokenToNew() throws Exception {
        // Arrange
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken token = inv.getArgument(0);
            if (token.getId() == null) {
                // Simular ID do novo token
                var idField = RefreshToken.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(token, 2L);
            }
            return token;
        });

        // Act
        refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.getReplacedByTokenId()).isEqualTo(2L);
        assertThat(validRefreshToken.getRevokedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar novo access token com tempo de expiração correto")
    void shouldGenerateAccessTokenWithCorrectExpiration() {
        // Arrange
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(jwtService.generateAccessToken(any())).thenReturn("new.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RefreshTokenResponse response = refreshTokenUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertThat(response.expiresIn()).isEqualTo(900L); // 15 minutos = 900 segundos
        verify(jwtService).generateAccessToken(validUser);
    }
}


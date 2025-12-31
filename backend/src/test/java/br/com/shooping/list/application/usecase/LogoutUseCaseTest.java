package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.LogoutRequest;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.infrastructure.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para LogoutUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase - Testes Unitários")
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    private LogoutRequest validRequest;
    private User validUser;
    private RefreshToken validRefreshToken;

    @BeforeEach
    void setUp() throws Exception {
        validRequest = new LogoutRequest("49a6336d-5649-466a-afeb-beee6b2f31d0");

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
    }

    @Test
    @DisplayName("Deve revogar refresh token com sucesso")
    void shouldLogoutSuccessfully() {
        // Arrange
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        assertThatNoException().isThrownBy(() -> logoutUseCase.execute(validRequest));

        // Assert - Token deve estar revogado
        assertThat(validRefreshToken.isRevoked()).isTrue();
        assertThat(validRefreshToken.getRevokedAt()).isNotNull();
        assertThat(validRefreshToken.getReplacedByTokenId()).isNull(); // Logout não tem replacement

        // Verify - Deve ter salvo o token revogado
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isRevoked()).isTrue();
        assertThat(savedToken.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token não encontrado")
    void shouldThrowExceptionWhenTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> logoutUseCase.execute(validRequest))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token inválido");

        // Verify - Não deve ter salvado nada
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar fazer logout com token já revogado")
    void shouldThrowExceptionWhenTokenAlreadyRevoked() throws Exception {
        // Arrange - Revogar o token manualmente
        validRefreshToken.revoke(null);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));

        // Act & Assert
        assertThatThrownBy(() -> logoutUseCase.execute(validRequest))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token já foi revogado");

        // Verify - Não deve ter salvado nada (pois já estava revogado)
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve revogar token mesmo se estiver expirado")
    void shouldRevokeExpiredToken() throws Exception {
        // Arrange - Criar token válido e depois modificar para expirado via reflexão
        RefreshToken expiredToken = RefreshToken.create(
                validUser,
                "hashDoTokenExpirado",
                Instant.now().plusSeconds(1000),
                "Mozilla/5.0",
                "192.168.1.1"
        );

        // Modificar data de expiração via reflexão para o passado
        var expiresAtField = RefreshToken.class.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(expiredToken, Instant.now().minusSeconds(1000)); // Expirado há 1000 segundos

        var tokenIdField = RefreshToken.class.getDeclaredField("id");
        tokenIdField.setAccessible(true);
        tokenIdField.set(expiredToken, 2L);

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        assertThatNoException().isThrownBy(() -> logoutUseCase.execute(validRequest));

        // Assert - Token expirado deve ser revogado normalmente
        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(expiredToken.isRevoked()).isTrue();
        assertThat(expiredToken.getRevokedAt()).isNotNull();

        // Verify
        verify(refreshTokenRepository).save(expiredToken);
    }

    @Test
    @DisplayName("Deve usar hash SHA-256 para buscar token")
    void shouldUseHashToFindToken() {
        // Arrange
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        logoutUseCase.execute(validRequest);

        // Assert - Deve ter buscado pelo hash (não pelo token original)
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).findByTokenHash(hashCaptor.capture());

        String capturedHash = hashCaptor.getValue();
        // Hash SHA-256 em Base64 deve ter tamanho específico
        assertThat(capturedHash).isNotEmpty();
        assertThat(capturedHash).isNotEqualTo(validRequest.getRefreshToken());
    }

    @Test
    @DisplayName("Deve validar que request não é nulo")
    void shouldValidateRequestNotNull() {
        // Act & Assert
        assertThatThrownBy(() -> logoutUseCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve lidar com token vazio")
    void shouldHandleEmptyToken() {
        // Arrange
        LogoutRequest emptyTokenRequest = new LogoutRequest("");

        // Act & Assert - TokenHashUtil deve lançar IllegalArgumentException
        assertThatThrownBy(() -> logoutUseCase.execute(emptyTokenRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve preservar dados do usuário ao revogar token")
    void shouldPreserveUserDataWhenRevoking() {
        // Arrange
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        logoutUseCase.execute(validRequest);

        // Assert - Dados do usuário devem permanecer intactos
        assertThat(validRefreshToken.getUser()).isEqualTo(validUser);
        assertThat(validRefreshToken.getUser().getEmail()).isEqualTo("usuario@email.com");
        assertThat(validRefreshToken.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(validRefreshToken.getIp()).isEqualTo("192.168.1.1");
    }
}


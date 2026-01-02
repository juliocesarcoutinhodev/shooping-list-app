package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.LoginRequest;
import br.com.shooping.list.application.dto.auth.LoginResponse;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.domain.user.UserStatus;
import br.com.shooping.list.infrastructure.exception.InvalidCredentialsException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para LoginUserUseCase
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase - Testes Unitários")
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private LoginUserUseCase loginUserUseCase;

    private LoginRequest validRequest;
    private User validUser;
    private JwtProperties.AccessToken accessTokenConfig;
    private JwtProperties.RefreshToken refreshTokenConfig;

    @BeforeEach
    void setUp() {
        validRequest = new LoginRequest("usuario@email.com", "senha@123");

        validUser = User.createLocalUser(
                "usuario@email.com",
                "João Silva",
                "$2a$10$hashedPassword"
        );

        // Configurar mocks de propriedades JWT
        accessTokenConfig = new JwtProperties.AccessToken();
        accessTokenConfig.setExpiration(Duration.ofMinutes(15));

        refreshTokenConfig = new JwtProperties.RefreshToken();
        refreshTokenConfig.setExpiration(Duration.ofDays(7));
    }

    @Test
    @DisplayName("Deve fazer login com sucesso quando credenciais válidas")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        // Simular ID do usuário
        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(validUser, 1L);

        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(validRequest.password(), validUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(validUser)).thenReturn("access.token.jwt");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LoginResponse response = loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access.token.jwt");
        assertThat(response.refreshToken()).isNotNull().isNotEmpty();
        assertThat(response.expiresIn()).isEqualTo(900L); // 15 minutos

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder).matches(validRequest.password(), validUser.getPasswordHash());
        verify(jwtService).generateAccessToken(validUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve persistir refresh token com hash SHA-256")
    void shouldPersistRefreshTokenWithHash() throws Exception {
        // Arrange
        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(validUser, 1L);

        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access.token.jwt");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LoginResponse response = loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getTokenHash()).isNotNull();
        assertThat(savedToken.getTokenHash()).isNotEqualTo(response.refreshToken()); // Hash é diferente do token
        assertThat(savedToken.getUser()).isEqualTo(validUser);
        assertThat(savedToken.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(savedToken.getIp()).isEqualTo("192.168.1.1");
        assertThat(savedToken.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Email ou senha não conferem");

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha incorreta")
    void shouldThrowExceptionWhenPasswordIncorrect() {
        // Arrange
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(validRequest.password(), validUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Email ou senha não conferem");

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder).matches(validRequest.password(), validUser.getPasswordHash());
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário está inativo")
    void shouldThrowExceptionWhenUserInactive() throws Exception {
        // Arrange
        // Criar usuário e setar status DISABLED
        var statusField = User.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(validUser, UserStatus.DISABLED);

        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(validRequest.password(), validUser.getPasswordHash())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Usuário inativo");

        // Verify
        verify(userRepository).findByEmail(validRequest.email());
        verify(passwordEncoder).matches(validRequest.password(), validUser.getPasswordHash());
        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve gerar tokens com tempo de expiração correto")
    void shouldGenerateTokensWithCorrectExpiration() throws Exception {
        // Arrange
        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(validUser, 1L);

        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access.token.jwt");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LoginResponse response = loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert
        assertThat(response.expiresIn()).isEqualTo(900L); // 15 minutos = 900 segundos

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getExpiresAt()).isAfter(java.time.Instant.now().plusSeconds(604700)); // ~7 dias
    }

    @Test
    @DisplayName("Refresh token deve ser UUID válido")
    void refreshTokenShouldBeValidUUID() throws Exception {
        // Arrange
        var idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(validUser, 1L);

        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any())).thenReturn("access.token.jwt");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        LoginResponse response = loginUserUseCase.execute(validRequest, "Mozilla/5.0", "192.168.1.1");

        // Assert - Deve ser possível converter para UUID
        assertThatNoException().isThrownBy(() -> java.util.UUID.fromString(response.refreshToken()));
    }
}


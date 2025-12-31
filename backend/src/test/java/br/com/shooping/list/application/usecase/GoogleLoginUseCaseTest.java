package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.LoginResponse;
import br.com.shooping.list.domain.user.*;
import br.com.shooping.list.infrastructure.security.*;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator.GoogleUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para GoogleLoginUseCase.
 * Valida o fluxo de autenticação via Google OAuth2.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GoogleLoginUseCase - Testes Unitários")
class GoogleLoginUseCaseTest {

    @Mock
    private GoogleTokenValidator googleTokenValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtProperties.AccessToken accessTokenConfig;

    @Mock
    private JwtProperties.RefreshToken refreshTokenConfig;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private GoogleLoginUseCase googleLoginUseCase;

    private static final String VALID_ID_TOKEN = "valid.google.id.token";
    private static final String GOOGLE_EMAIL = "user@gmail.com";
    private static final String GOOGLE_NAME = "Google User";
    private static final String GOOGLE_ID = "google-sub-123";
    private static final String ACCESS_TOKEN = "generated.access.token";
    private static final String REFRESH_TOKEN_VALUE = "generated-refresh-token";
    private static final String REFRESH_TOKEN_HASH = "hashed-refresh-token";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String CLIENT_IP = "192.168.1.1";

    @BeforeEach
    void setUp() {
        // Configura mocks de propriedades
        when(jwtProperties.getAccessToken()).thenReturn(accessTokenConfig);
        when(accessTokenConfig.getExpiration()).thenReturn(Duration.ofHours(1));

        when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenConfig);
        when(refreshTokenConfig.getExpiration()).thenReturn(Duration.ofDays(7));

        // Configura request
        when(httpRequest.getHeader("User-Agent")).thenReturn(USER_AGENT);
        when(httpRequest.getRemoteAddr()).thenReturn(CLIENT_IP);
    }

    @Test
    @DisplayName("Deve fazer login com Google para usuário existente")
    void shouldLoginWithGoogleForExistingUser() {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        User existingUser = User.createGoogleUser(GOOGLE_EMAIL, GOOGLE_NAME);
        existingUser.addRole(createUserRole());

        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn(ACCESS_TOKEN);

        try (MockedStatic<TokenHashUtil> hashUtilMock = mockStatic(TokenHashUtil.class)) {
            hashUtilMock.when(() -> TokenHashUtil.hashToken(anyString())).thenReturn(REFRESH_TOKEN_HASH);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isNotNull();
            // Valida que é um UUID válido
            assertThat(response.getRefreshToken()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);

            verify(googleTokenValidator).validate(VALID_ID_TOKEN);
            verify(userRepository).findByEmail(GOOGLE_EMAIL);
            verify(userRepository, never()).save(any()); // Não cria novo usuário
            verify(jwtService).generateAccessToken(existingUser);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Test
    @DisplayName("Deve provisionar novo usuário Google e fazer login")
    void shouldProvisionNewGoogleUserAndLogin() {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        Role userRole = createUserRole();
        User newUser = User.createGoogleUser(GOOGLE_EMAIL, GOOGLE_NAME);
        newUser.addRole(userRole);

        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);

        try (MockedStatic<TokenHashUtil> hashUtilMock = mockStatic(TokenHashUtil.class)) {
            hashUtilMock.when(() -> TokenHashUtil.hashToken(anyString())).thenReturn(REFRESH_TOKEN_HASH);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isNotNull();
            // Valida que é um UUID válido
            assertThat(response.getRefreshToken()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);

            verify(googleTokenValidator).validate(VALID_ID_TOKEN);
            verify(userRepository).findByEmail(GOOGLE_EMAIL);
            verify(roleRepository).findByName("USER");
            verify(userRepository).save(any(User.class)); // Provisiona novo usuário
            verify(jwtService).generateAccessToken(any(User.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Test
    @DisplayName("Deve lançar exceção quando token do Google for inválido")
    void shouldThrowExceptionWhenGoogleTokenIsInvalid() {
        // Given
        when(googleTokenValidator.validate(VALID_ID_TOKEN))
                .thenThrow(new GoogleTokenValidationException("Token inválido"));

        // When / Then
        assertThatThrownBy(() -> googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest))
                .isInstanceOf(GoogleTokenValidationException.class)
                .hasMessageContaining("Token inválido");

        verify(googleTokenValidator).validate(VALID_ID_TOKEN);
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando role USER não for encontrada")
    void shouldThrowExceptionWhenUserRoleNotFound() {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);

        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role USER não encontrada");

        verify(googleTokenValidator).validate(VALID_ID_TOKEN);
        verify(userRepository).findByEmail(GOOGLE_EMAIL);
        verify(roleRepository).findByName("USER");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve extrair IP de X-Forwarded-For quando presente")
    void shouldExtractIpFromXForwardedForHeader() {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        User existingUser = User.createGoogleUser(GOOGLE_EMAIL, GOOGLE_NAME);
        existingUser.addRole(createUserRole());

        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2");
        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn(ACCESS_TOKEN);

        try (MockedStatic<TokenHashUtil> hashUtilMock = mockStatic(TokenHashUtil.class)) {
            hashUtilMock.when(() -> TokenHashUtil.hashToken(anyString())).thenReturn(REFRESH_TOKEN_HASH);

            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest);

            // Then
            assertThat(response).isNotNull();
            verify(httpRequest).getHeader("X-Forwarded-For");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Test
    @DisplayName("Deve criar refresh token com metadata correto")
    void shouldCreateRefreshTokenWithCorrectMetadata() {
        // Given
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(GOOGLE_EMAIL, GOOGLE_NAME, GOOGLE_ID, true);
        User existingUser = User.createGoogleUser(GOOGLE_EMAIL, GOOGLE_NAME);
        existingUser.addRole(createUserRole());

        when(googleTokenValidator.validate(VALID_ID_TOKEN)).thenReturn(googleUserInfo);
        when(userRepository.findByEmail(GOOGLE_EMAIL)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn(ACCESS_TOKEN);

        try (MockedStatic<TokenHashUtil> hashUtilMock = mockStatic(TokenHashUtil.class)) {
            hashUtilMock.when(() -> TokenHashUtil.hashToken(anyString())).thenReturn(REFRESH_TOKEN_HASH);

            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoginResponse response = googleLoginUseCase.execute(VALID_ID_TOKEN, httpRequest);

            // Then
            assertThat(response).isNotNull();

            verify(refreshTokenRepository).save(argThat(token -> {
                assertThat(token.getTokenHash()).isEqualTo(REFRESH_TOKEN_HASH);
                assertThat(token.getUserAgent()).isEqualTo(USER_AGENT);
                assertThat(token.getIp()).isEqualTo(CLIENT_IP);
                assertThat(token.getExpiresAt()).isAfter(Instant.now());
                return true;
            }));
        }
    }

    /**
     * Helper para criar role USER para testes
     */
    private Role createUserRole() {
        return Role.create("USER", "Usuário padrão");
    }
}


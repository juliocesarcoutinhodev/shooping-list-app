package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.LoginResponse;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator;
import br.com.shooping.list.infrastructure.security.GoogleTokenValidator.GoogleUserInfo;
import br.com.shooping.list.infrastructure.security.JwtProperties;
import br.com.shooping.list.infrastructure.security.JwtService;
import br.com.shooping.list.infrastructure.security.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Caso de uso para autenticação via Google OAuth2.
 * Valida o token do Google, provisiona o usuário se não existir e emite tokens JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleLoginUseCase {

    private final GoogleTokenValidator googleTokenValidator;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse execute(String idToken, HttpServletRequest request) {
        log.info("Iniciando login via Google OAuth2");

        // Valida token do Google e extrai informações do usuário
        GoogleUserInfo googleUserInfo = googleTokenValidator.validate(idToken);
        log.info("Token do Google validado: email={}, googleId={}", googleUserInfo.email(), googleUserInfo.googleId());

        // Busca ou cria usuário
        User user = userRepository.findByEmail(googleUserInfo.email())
                .map(existingUser -> {
                    log.info("Usuário Google já existe: userId={}, email={}", existingUser.getId(), existingUser.getEmail());
                    return existingUser;
                })
                .orElseGet(() -> {
                    log.info("Provisionando novo usuário Google: email={}", googleUserInfo.email());
                    return provisionGoogleUser(googleUserInfo);
                });

        // Gera access token
        String accessToken = jwtService.generateAccessToken(user);
        long expiresIn = jwtProperties.getAccessToken().getExpiration().getSeconds();

        // Gera refresh token (UUID)
        String refreshTokenValue = UUID.randomUUID().toString();

        // Faz hash do refresh token (SHA-256)
        String refreshTokenHash = TokenHashUtil.hashToken(refreshTokenValue);

        // Calcula expiração do refresh token
        Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshToken().getExpiration());

        // Extrai metadados da requisição
        String userAgent = request.getHeader("User-Agent");
        String ip = extractIpAddress(request);

        // Persiste refresh token
        RefreshToken refreshToken = RefreshToken.create(
                user,
                refreshTokenHash,
                expiresAt,
                userAgent,
                ip
        );
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token criado para userId={}, expiresAt={}", user.getId(), expiresAt);
        log.info("Login via Google realizado com sucesso para userId={}, email={}", user.getId(), user.getEmail());

        return new LoginResponse(
                accessToken,
                refreshTokenValue,
                expiresIn
        );
    }

    /**
     * Provisiona um novo usuário a partir dos dados do Google.
     */
    private User provisionGoogleUser(GoogleUserInfo googleUserInfo) {
        User newUser = User.createGoogleUser(googleUserInfo.email(), googleUserInfo.name());

        // Atribui role padrão USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER não encontrada"));
        newUser.addRole(userRole);

        User savedUser = userRepository.save(newUser);
        log.info("Novo usuário Google provisionado: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * Extrai o endereço IP real do cliente, considerando proxies.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}


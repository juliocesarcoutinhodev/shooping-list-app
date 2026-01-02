package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.RefreshTokenRequest;
import br.com.shooping.list.application.dto.auth.RefreshTokenResponse;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.infrastructure.exception.InvalidRefreshTokenException;
import br.com.shooping.list.infrastructure.security.JwtProperties;
import br.com.shooping.list.infrastructure.security.JwtService;
import br.com.shooping.list.infrastructure.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Caso de uso: Renovação de access token via refresh token
 *
 * Responsabilidades:
 * - Validar refresh token (hash, expiração, revogação)
 * - Detectar reuso de token revogado (segurança)
 * - Rotacionar refresh token (gerar novo e revogar antigo)
 * - Gerar novo access token (JWT)
 * - Retornar novos tokens
 *
 * Segurança:
 * - Refresh token é usado UMA ÚNICA VEZ (rotação automática)
 * - Reuso de token revogado pode indicar comprometimento
 * - Token antigo fica vinculado ao novo via replacedByTokenId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshTokenResponse execute(RefreshTokenRequest request, String userAgent, String ip) {
        log.info("Tentativa de refresh token");

        // 1. Fazer hash do token recebido
        String tokenHash = TokenHashUtil.hashToken(request.refreshToken());

        // 2. Buscar token no banco pelo hash
        var currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token não encontrado");
                    return new InvalidRefreshTokenException("Refresh token inválido");
                });

        // 3. Validar se o token foi revogado (REUSO DETECTADO - possível ataque!)
        if (currentToken.isRevoked()) {
            log.error("ALERTA DE SEGURANÇA: Tentativa de reuso de refresh token revogado! tokenId={}, userId={}",
                    currentToken.getId(), currentToken.getUser().getId());

            // Opcionalmente, revogar toda a cadeia de tokens do usuário aqui
            // revokeTokenChain(currentToken);

            throw new InvalidRefreshTokenException("Refresh token já foi utilizado");
        }

        // 4. Validar se o token está expirado
        if (currentToken.isExpired()) {
            log.warn("Refresh token expirado: tokenId={}, expiresAt={}",
                    currentToken.getId(), currentToken.getExpiresAt());
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        var user = currentToken.getUser();
        log.info("Refresh token válido para userId={}, email={}", user.getId(), user.getEmail());

        // 5. Gerar novo access token (JWT)
        String newAccessToken = jwtService.generateAccessToken(user);
        long expiresIn = jwtProperties.getAccessToken().getExpiration().getSeconds();

        // 6. Gerar novo refresh token (UUID) - ROTAÇÃO
        String newRefreshTokenValue = UUID.randomUUID().toString();
        String newRefreshTokenHash = TokenHashUtil.hashToken(newRefreshTokenValue);

        // 7. Calcular nova expiração
        Instant newExpiration = Instant.now()
                .plus(jwtProperties.getRefreshToken().getExpiration());

        // 8. Criar e persistir o NOVO refresh token
        var newRefreshToken = RefreshToken.create(
                user,
                newRefreshTokenHash,
                newExpiration,
                userAgent,
                ip
        );
        var savedNewToken = refreshTokenRepository.save(newRefreshToken);

        log.info("Novo refresh token criado: tokenId={}, userId={}, expiresAt={}",
                savedNewToken.getId(), user.getId(), newExpiration);

        // 9. Revogar o token ANTIGO e vincular ao novo
        currentToken.revoke(savedNewToken.getId());
        refreshTokenRepository.save(currentToken);

        log.info("Refresh token antigo revogado: tokenId={}, replacedBy={}",
                currentToken.getId(), savedNewToken.getId());
        log.info("Refresh token rotacionado com sucesso para userId={}", user.getId());

        // 10. Retornar novos tokens
        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshTokenValue,
                expiresIn
        );
    }
}


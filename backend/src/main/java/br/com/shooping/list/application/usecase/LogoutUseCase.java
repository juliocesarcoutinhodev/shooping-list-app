package br.com.shooping.list.application.usecase;

import br.com.shooping.list.application.dto.auth.LogoutRequest;
import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import br.com.shooping.list.infrastructure.exception.InvalidRefreshTokenException;
import br.com.shooping.list.infrastructure.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Caso de uso: Logout (revogação de refresh token)
 *
 * Responsabilidades:
 * - Validar refresh token recebido
 * - Revogar o refresh token (marcando como revoked)
 * - Impedir que o token seja usado novamente
 *
 * Segurança:
 * - Após logout, o refresh token não pode mais ser usado
 * - Token revogado não pode ser revogado novamente
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(LogoutRequest request) {
        log.info("Tentativa de logout");

        // 1. Fazer hash do token recebido
        String tokenHash = TokenHashUtil.hashToken(request.getRefreshToken());

        // 2. Buscar token no banco pelo hash
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token não encontrado para logout");
                    return new InvalidRefreshTokenException("Refresh token inválido");
                });

        // 3. Verificar se o token já foi revogado
        if (currentToken.isRevoked()) {
            log.warn("Tentativa de logout com token já revogado: tokenId={}", currentToken.getId());
            throw new InvalidRefreshTokenException("Refresh token já foi revogado");
        }

        // 4. Revogar o token (sem replacement, pois é logout)
        currentToken.revoke(null);
        refreshTokenRepository.save(currentToken);

        log.info("Logout realizado com sucesso: tokenId={}, userId={}",
                currentToken.getId(), currentToken.getUser().getId());
    }
}


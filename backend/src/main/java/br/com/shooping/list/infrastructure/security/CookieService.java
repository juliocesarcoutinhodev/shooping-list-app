package br.com.shooping.list.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Serviço para gerenciar cookies de refresh token com segurança
 * Suporta configuração por perfil (dev/test/prod)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final RefreshTokenCookieProperties cookieProperties;

    /**
     * Cria e adiciona cookie de refresh token na resposta HTTP
     *
     * @param response resposta HTTP onde o cookie será adicionado
     * @param refreshToken valor do refresh token (UUID)
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        log.debug("Criando cookie de refresh token: name={}, httpOnly={}, secure={}, sameSite={}, maxAge={}s",
                cookieProperties.getName(),
                cookieProperties.isHttpOnly(),
                cookieProperties.isSecure(),
                cookieProperties.getSameSite(),
                cookieProperties.getMaxAge());

        // Usa ResponseCookie (Spring) para suporte completo a SameSite
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getName(), refreshToken)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(cookieProperties.getMaxAge())
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain()) // null = usa domínio da requisição
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        log.info("Cookie de refresh token adicionado com sucesso");
    }

    /**
     * Remove o cookie de refresh token (usado no logout)
     *
     * @param response resposta HTTP onde o cookie será removido
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        log.debug("Removendo cookie de refresh token: name={}", cookieProperties.getName());

        // Cookie com maxAge=0 remove o cookie do navegador
        ResponseCookie cookie = ResponseCookie.from(cookieProperties.getName(), "")
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(0) // Remove imediatamente
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain())
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        log.info("Cookie de refresh token removido com sucesso");
    }

    /**
     * Extrai o refresh token do cookie da requisição
     *
     * @param request requisição HTTP
     * @return refresh token se presente, ou Optional.empty()
     */
    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            log.debug("Nenhum cookie presente na requisição");
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieProperties.getName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .map(token -> {
                    log.debug("Refresh token encontrado no cookie");
                    return token;
                });
    }

    /**
     * Verifica se o modo cookie-only está ativado
     *
     * @return true se refresh token deve ser enviado APENAS via cookie
     */
    public boolean isCookieOnly() {
        return cookieProperties.isCookieOnly();
    }
}


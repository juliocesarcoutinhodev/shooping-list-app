package br.com.shooping.list.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurações do cookie de refresh token
 * Permite configuração diferente por perfil (dev/test/prod)
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.refresh-token.cookie")
@Getter
@Setter
public class RefreshTokenCookieProperties {

    /**
     * Nome do cookie que armazenará o refresh token
     * Default: "refreshToken"
     */
    private String name = "refreshToken";

    /**
     * Se true, envia cookie com flag HttpOnly (JavaScript não pode acessar)
     * Default: true (sempre recomendado por segurança)
     */
    private boolean httpOnly = true;

    /**
     * Se true, envia cookie apenas via HTTPS
     * Default: false (deve ser true em produção)
     */
    private boolean secure = false;

    /**
     * Path do cookie (restringe onde o cookie é enviado)
     * Default: "/api/v1/auth" (apenas endpoints de autenticação)
     */
    private String path = "/api/v1/auth";

    /**
     * Política SameSite para proteção CSRF
     * Valores: "Strict", "Lax", "None"
     * Default: "Lax"
     */
    private String sameSite = "Lax";

    /**
     * Tempo de vida do cookie em segundos
     * Default: 604800 (7 dias)
     * Deve coincidir com a expiração do refresh token
     */
    private int maxAge = 604800; // 7 dias

    /**
     * Se true, refresh token é retornado APENAS via cookie (mais seguro)
     * Se false, refresh token também vai no body da resposta (backward compatibility)
     * Default: false (permite body em dev/test)
     */
    private boolean cookieOnly = false;

    /**
     * Domínio do cookie (para compartilhar entre subdomínios)
     * Default: null (usa o domínio da requisição)
     */
    private String domain = null;
}


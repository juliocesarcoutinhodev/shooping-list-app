package br.com.shooping.list.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Propriedades de configuração JWT externalizadas via application.yml
 * Permite configurar segredo, issuer e tempos de expiração por profile
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Chave secreta para assinatura JWT (mínimo 256 bits para HS256)
     * Deve ser configurada via variável de ambiente JWT_SECRET
     */
    private String secret;

    /**
     * Identificador do emissor do token (issuer claim)
     */
    private String issuer = "shopping-list-api";

    /**
     * Configurações do Access Token
     */
    private AccessToken accessToken = new AccessToken();

    /**
     * Configurações do Refresh Token
     */
    private RefreshToken refreshToken = new RefreshToken();

    @Getter
    @Setter
    public static class AccessToken {
        /**
         * Tempo de expiração do access token
         * Exemplos: 15m, 1h, 2h
         */
        private Duration expiration = Duration.ofHours(1);
    }

    @Getter
    @Setter
    public static class RefreshToken {
        /**
         * Tempo de expiração do refresh token
         * Exemplos: 7d, 30d
         */
        private Duration expiration = Duration.ofDays(7);
    }
}


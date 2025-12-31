package br.com.shooping.list.infrastructure.security;

/**
 * Exception lançada quando o token do Google OAuth2 é inválido.
 * Pode ocorrer por token expirado, malformado, assinatura inválida, etc.
 */
public class GoogleTokenValidationException extends RuntimeException {

    public GoogleTokenValidationException(String message) {
        super(message);
    }

    public GoogleTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


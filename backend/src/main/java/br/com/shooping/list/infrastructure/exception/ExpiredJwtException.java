package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando um token JWT está expirado
 */
public class ExpiredJwtException extends RuntimeException {

    public ExpiredJwtException(String message) {
        super(message);
    }

    public ExpiredJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}


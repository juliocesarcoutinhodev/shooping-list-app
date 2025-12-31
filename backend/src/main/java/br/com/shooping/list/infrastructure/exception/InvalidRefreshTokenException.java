package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando o refresh token é inválido
 * (não encontrado, expirado, revogado ou reutilizado)
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}


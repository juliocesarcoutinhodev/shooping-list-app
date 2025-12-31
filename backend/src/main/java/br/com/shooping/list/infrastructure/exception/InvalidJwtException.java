package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando um token JWT é inválido
 * Pode incluir: assinatura inválida, token malformado, claims inválidos, etc.
 */
public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message) {
        super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}


package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando as credenciais de login são inválidas
 * (email não encontrado, senha incorreta ou usuário inativo)
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}


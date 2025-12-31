package br.com.shooping.list.infrastructure.exception;

/**
 * Exceção lançada quando se tenta registrar um usuário com email já existente
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' já está cadastrado", email));
    }
}


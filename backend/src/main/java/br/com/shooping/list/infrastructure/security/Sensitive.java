package br.com.shooping.list.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para marcar campos sensíveis que não devem aparecer em logs.
 * <p>
 * Exemplos de campos sensíveis:
 * - Senhas
 * - Tokens (JWT, refresh tokens, API keys)
 * - Dados pessoais sensíveis (CPF, cartão de crédito)
 * - Chaves criptográficas
 * <p>
 * Uso:
 * <pre>
 * public class LoginRequest {
 *     private String email;
 *
 *     &#64;Sensitive
 *     private String password;
 * }
 * </pre>
 * <p>
 * DTOs anotados devem implementar toString() usando {@link LogSanitizer#sanitize(Object)}
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
}


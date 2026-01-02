package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.infrastructure.security.LogSanitizer;
import br.com.shooping.list.infrastructure.security.Sensitive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login de usuário LOCAL
 */
public record LoginRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,

        @Sensitive
        @NotBlank(message = "Senha é obrigatória")
        String password
) {
    @Override
    public String toString() {
        return LogSanitizer.sanitize(this);
    }
}


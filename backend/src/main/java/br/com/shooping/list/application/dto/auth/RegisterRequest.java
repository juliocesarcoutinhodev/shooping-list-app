package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.infrastructure.security.LogSanitizer;
import br.com.shooping.list.infrastructure.security.Sensitive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro de novo usuário LOCAL
 */
public record RegisterRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String name,

        @Sensitive
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String password
) {
    @Override
    public String toString() {
        return LogSanitizer.sanitize(this);
    }
}


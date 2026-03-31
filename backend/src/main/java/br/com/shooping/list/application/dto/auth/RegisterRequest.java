package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.infrastructure.security.LogSanitizer;
import br.com.shooping.list.infrastructure.security.Sensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de registro de novo usuário LOCAL
 */
@Schema(
    name = "AuthRegisterRequest",
    description = "User registration data for creating a new LOCAL account"
)
public record RegisterRequest(
        @Schema(
            description = "User email address (must be unique)",
            example = "newuser@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
        )
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,

        @Schema(
            description = "User full name",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 150
        )
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String name,

        @Schema(
            description = "User password (will be hashed with BCrypt)",
            example = "MySecureP@ssw0rd123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 100,
            accessMode = Schema.AccessMode.WRITE_ONLY
        )
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


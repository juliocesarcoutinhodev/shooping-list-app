package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.infrastructure.security.LogSanitizer;
import br.com.shooping.list.infrastructure.security.Sensitive;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login de usuário LOCAL
 */
@Schema(
    name = "AuthLoginRequest",
    description = "Login credentials for LOCAL authentication (email + password)"
)
public record LoginRequest(
        @Schema(
            description = "User email address",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,

        @Schema(
            description = "User password",
            example = "MySecureP@ssw0rd",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @Sensitive
        @NotBlank(message = "Senha é obrigatória")
        String password
) {
    @Override
    public String toString() {
        return LogSanitizer.sanitize(this);
    }
}


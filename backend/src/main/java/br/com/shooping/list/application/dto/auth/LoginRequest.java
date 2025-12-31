package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.infrastructure.security.LogSanitizer;
import br.com.shooping.list.infrastructure.security.Sensitive;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login de usuário LOCAL
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @Sensitive
    @NotBlank(message = "Senha é obrigatória")
    private String password;

    @Override
    public String toString() {
        return LogSanitizer.sanitize(this);
    }
}


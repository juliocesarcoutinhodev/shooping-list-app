package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO para resposta de registro de usuário
 * Não expõe dados sensíveis como password hash
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long id;
    private String email;
    private String name;
    private AuthProvider provider;
    private UserStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
}


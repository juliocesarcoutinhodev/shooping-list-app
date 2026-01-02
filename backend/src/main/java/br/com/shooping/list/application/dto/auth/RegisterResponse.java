package br.com.shooping.list.application.dto.auth;

import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * DTO para resposta de registro de usuário
 * Não expõe dados sensíveis como password hash
 */
public record RegisterResponse(
        Long id,
        String email,
        String name,
        AuthProvider provider,
        UserStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt
) {}


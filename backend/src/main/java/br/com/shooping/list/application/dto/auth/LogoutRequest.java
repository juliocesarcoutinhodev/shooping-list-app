package br.com.shooping.list.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requisição para logout
 */
@Schema(
    name = "AuthLogoutRequest",
    description = "Refresh token to revoke during logout (optional if sent via HttpOnly cookie)"
)
public record LogoutRequest(
        @Schema(
            description = "Refresh token to be revoked. Can be sent via cookie instead of body.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}


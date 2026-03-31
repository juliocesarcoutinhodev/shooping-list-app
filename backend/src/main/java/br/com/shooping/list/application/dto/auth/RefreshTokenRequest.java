package br.com.shooping.list.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de renovação de access token
 */
@Schema(
    name = "AuthRefreshRequest",
    description = "Refresh token for renewing access token (optional if sent via HttpOnly cookie)"
)
public record RefreshTokenRequest(
        @Schema(
            description = "Refresh token (JWT). Can be sent via cookie instead of body.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}


package br.com.shooping.list.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para resposta de renovação de token
 * Contém novo access token, novo refresh token e tempo de expiração
 *
 * Nota: O refresh token é rotacionado (o antigo é revogado)
 */
@Schema(
    name = "AuthRefreshResponse",
    description = "New JWT tokens returned after refresh (old refresh token is revoked)"
)
public record RefreshTokenResponse(
        @Schema(
            description = "New JWT access token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String accessToken,

        @Schema(
            description = "New JWT refresh token (rotated). May be null if cookie-only mode enabled.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            accessMode = Schema.AccessMode.READ_ONLY,
            nullable = true
        )
        String refreshToken,

        @Schema(
            description = "New access token expiration time in seconds",
            example = "900",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Long expiresIn
) {}


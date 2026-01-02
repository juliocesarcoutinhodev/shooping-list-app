package br.com.shooping.list.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para resposta de login
 * Contém access token, refresh token e tempo de expiração
 */
@Schema(
    name = "AuthTokensResponse",
    description = "JWT tokens returned after successful authentication"
)
public record LoginResponse(
        @Schema(
            description = "JWT access token (short-lived, use for API requests)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDA5OTYxMDB9...",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String accessToken,

        @Schema(
            description = "JWT refresh token (long-lived, use to get new access token). May be null if cookie-only mode enabled.",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDE2MDAwMDB9...",
            accessMode = Schema.AccessMode.READ_ONLY,
            nullable = true
        )
        String refreshToken,

        @Schema(
            description = "Access token expiration time in seconds",
            example = "900",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Long expiresIn
) {}


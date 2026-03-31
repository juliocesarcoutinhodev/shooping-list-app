package br.com.shooping.list.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login via Google OAuth2.
 * Recebe o idToken emitido pelo Google após autenticação do usuário.
 */
@Schema(
    name = "AuthGoogleLoginRequest",
    description = "Google OAuth2 ID Token for authentication"
)
public record GoogleLoginRequest(
        @Schema(
            description = "Google ID Token issued after user authentication",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAiLCJ0eXAiOiJKV1QifQ...",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY
        )
        @NotBlank(message = "ID Token do Google é obrigatório")
        String idToken
) {}


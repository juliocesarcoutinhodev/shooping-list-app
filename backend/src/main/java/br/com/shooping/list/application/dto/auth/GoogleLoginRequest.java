package br.com.shooping.list.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login via Google OAuth2.
 * Recebe o idToken emitido pelo Google após autenticação do usuário.
 */
public record GoogleLoginRequest(
        @NotBlank(message = "ID Token do Google é obrigatório")
        String idToken
) {}


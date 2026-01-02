package br.com.shooping.list.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de renovação de access token
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}


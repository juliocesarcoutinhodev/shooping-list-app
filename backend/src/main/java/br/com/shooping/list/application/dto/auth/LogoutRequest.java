package br.com.shooping.list.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requisição para logout
 */
public record LogoutRequest(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken
) {}


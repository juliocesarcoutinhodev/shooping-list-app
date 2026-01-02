package br.com.shooping.list.application.dto.auth;

/**
 * DTO para resposta de login
 * Contém access token, refresh token e tempo de expiração
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}


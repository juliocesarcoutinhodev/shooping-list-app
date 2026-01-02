package br.com.shooping.list.application.dto.auth;

/**
 * DTO para resposta de renovação de token
 * Contém novo access token, novo refresh token e tempo de expiração
 *
 * Nota: O refresh token é rotacionado (o antigo é revogado)
 */
public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}


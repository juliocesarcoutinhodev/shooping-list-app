package br.com.shooping.list.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de renovação de token
 * Contém novo access token, novo refresh token e tempo de expiração
 *
 * Nota: O refresh token é rotacionado (o antigo é revogado)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenResponse {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}


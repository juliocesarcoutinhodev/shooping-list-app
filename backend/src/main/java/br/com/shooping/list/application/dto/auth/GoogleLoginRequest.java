package br.com.shooping.list.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login via Google OAuth2.
 * Recebe o idToken emitido pelo Google após autenticação do usuário.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "ID Token do Google é obrigatório")
    private String idToken;
}


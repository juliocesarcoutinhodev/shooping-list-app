package br.com.shooping.list.infrastructure.security;

/**
 * Contrato para validação de tokens do Google OAuth2.
 * Permite abstrair a implementação real para facilitar testes com mocks.
 */
public interface GoogleTokenValidator {

    /**
     * Valida o ID Token do Google e retorna os dados do usuário.
     *
     * @param idToken token JWT emitido pelo Google
     * @return dados do usuário extraídos do token
     * @throws GoogleTokenValidationException se o token for inválido, expirado ou malformado
     */
    GoogleUserInfo validate(String idToken);

    /**
     * DTO interno com informações do usuário extraídas do token do Google.
     */
    record GoogleUserInfo(
            String email,
            String name,
            String googleId,
            boolean emailVerified
    ) {
    }
}


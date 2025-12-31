package br.com.shooping.list.infrastructure.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Implementação real da validação de tokens do Google OAuth2.
 * Utiliza a biblioteca oficial do Google para verificar a assinatura e validade do token.
 */
@Component
@Slf4j
public class GoogleTokenValidatorImpl implements GoogleTokenValidator {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenValidatorImpl(@Value("${app.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();

        log.info("GoogleTokenValidator inicializado com clientId configurado");
    }

    @Override
    public GoogleUserInfo validate(String idToken) {
        try {
            log.debug("Validando token do Google");

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                log.warn("Token do Google inválido ou expirado");
                throw new GoogleTokenValidationException("Token do Google inválido ou expirado");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();

            if (!emailVerified) {
                log.warn("Email não verificado no Google: {}", email);
                throw new GoogleTokenValidationException("Email não verificado pelo Google");
            }

            log.info("Token do Google validado com sucesso: email={}, googleId={}", email, googleId);

            return new GoogleUserInfo(email, name, googleId, emailVerified);

        } catch (GoogleTokenValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao validar token do Google", e);
            throw new GoogleTokenValidationException("Erro ao validar token do Google: " + e.getMessage(), e);
        }
    }
}


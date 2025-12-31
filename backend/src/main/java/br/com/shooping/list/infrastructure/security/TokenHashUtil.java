package br.com.shooping.list.infrastructure.security;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utilitário para hash de tokens usando SHA-256
 * <p>
 * Centraliza a lógica de geração de hash para tokens (refresh tokens, etc),
 * evitando duplicação de código nos use cases.
 * <p>
 * O hash é usado para armazenar tokens de forma segura no banco de dados,
 * garantindo que mesmo se o banco for comprometido, os tokens originais
 * não possam ser recuperados.
 */
@Slf4j
public final class TokenHashUtil {

    // Classe utilitária - construtor privado
    private TokenHashUtil() {
        throw new UnsupportedOperationException("Classe utilitária não pode ser instanciada");
    }

    /**
     * Gera hash SHA-256 do token para armazenamento seguro
     *
     * @param token token original a ser hashado
     * @return hash Base64 do token
     * @throws RuntimeException se ocorrer erro ao gerar hash (algoritmo não disponível)
     */
    public static String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser nulo ou vazio");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Erro ao gerar hash do token: algoritmo SHA-256 não disponível", e);
            throw new RuntimeException("Erro ao processar token", e);
        }
    }
}


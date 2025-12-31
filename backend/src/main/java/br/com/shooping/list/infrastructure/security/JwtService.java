package br.com.shooping.list.infrastructure.security;

import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.infrastructure.exception.ExpiredJwtException;
import br.com.shooping.list.infrastructure.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pela geração e validação de JWT (Access Tokens)
 * Centraliza toda a lógica de criação, assinatura e validação de tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    /**
     * Gera um access token JWT para o usuário autenticado
     * Inclui roles como claim para evitar consulta ao banco em cada requisição
     *
     * @param user usuário autenticado
     * @return token JWT assinado
     */
    public String generateAccessToken(User user) {
        log.debug("Gerando access token para userId={}, email={}", user.getId(), user.getEmail());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("name", user.getName());
        claims.put("provider", user.getProvider().name());

        // Inclui roles no token para propagação no SecurityContext
        // Converte Set<Role> para List<String> com apenas os nomes
        var roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        claims.put("roles", roleNames);

        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getAccessToken().getExpiration());

        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();

        log.debug("Access token gerado com sucesso. Expira em: {}. Roles incluídas: {}", expiration, roleNames);
        return token;
    }

    /**
     * Extrai o userId (subject) do token JWT
     *
     * @param token token JWT
     * @return userId como String
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrai o email do token JWT
     *
     * @param token token JWT
     * @return email do usuário
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extrai o nome do token JWT
     *
     * @param token token JWT
     * @return nome do usuário
     */
    public String extractName(String token) {
        return extractAllClaims(token).get("name", String.class);
    }

    /**
     * Extrai as roles do token JWT
     * Retorna lista de nomes das roles (ex: ["USER", "ADMIN"])
     *
     * @param token token JWT
     * @return lista de nomes das roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");

        // Se não houver roles no token, retorna lista vazia
        if (rolesObj == null) {
            log.warn("Token não contém claim 'roles'. Retornando lista vazia.");
            return List.of();
        }

        // JJWT deserializa arrays JSON como List<String>
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }

        log.warn("Claim 'roles' não é uma lista. Tipo: {}. Retornando lista vazia.",
                rolesObj.getClass().getSimpleName());
        return List.of();
    }

    /**
     * Gera um access token JWT com tempo de expiração customizado.
     * <p>
     * Útil para testes de tokens expirados.
     *
     * @param userId ID do usuário
     * @param email email do usuário
     * @param name nome do usuário
     * @param provider provedor de autenticação
     * @param expirationMillis tempo de expiração em milissegundos
     * @return token JWT assinado
     */
    public String generateAccessTokenWithCustomExpiration(
            Long userId,
            String email,
            String name,
            String provider,
            Long expirationMillis
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        claims.put("provider", provider);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida se o token é válido (assinatura correta e não expirado)
     *
     * @param token token JWT a ser validado
     * @throws ExpiredJwtException se o token estiver expirado
     * @throws InvalidJwtException se o token for inválido (assinatura, formato, etc)
     */
    public void validateToken(String token) {
        try {
            extractAllClaims(token);
            log.debug("Token validado com sucesso");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            throw new ExpiredJwtException("Token JWT expirado", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Assinatura inválida: {}", e.getMessage());
            throw new InvalidJwtException("Assinatura do token JWT inválida", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
            throw new InvalidJwtException("Token JWT malformado", e);
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            throw new InvalidJwtException("Erro ao validar token JWT", e);
        }
    }

    /**
     * Extrai todos os claims do token JWT
     *
     * @param token token JWT
     * @return Claims extraídos
     * @throws io.jsonwebtoken.JwtException se token inválido, expirado ou malformado
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gera a chave de assinatura a partir do secret configurado
     *
     * @return SecretKey para assinatura HMAC
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}


package br.com.shooping.list.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de repositório para RefreshToken (Port - Clean Architecture).
 * <p>
 * Define as operações de persistência necessárias para a entidade RefreshToken,
 * sem depender de detalhes de infraestrutura (JPA, JDBC, etc).
 * <p>
 * A implementação concreta fica na camada infrastructure.
 */
public interface RefreshTokenRepository {

    /**
     * Salva um refresh token (insert ou update).
     *
     * @param refreshToken refresh token a ser salvo
     * @return refresh token salvo com ID preenchido
     */
    RefreshToken save(RefreshToken refreshToken);

    /**
     * Busca um refresh token pelo hash.
     *
     * @param tokenHash hash do token
     * @return Optional com refresh token se encontrado, empty caso contrário
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Busca um refresh token por ID.
     *
     * @param id ID do refresh token
     * @return Optional com refresh token se encontrado, empty caso contrário
     */
    Optional<RefreshToken> findById(Long id);

    /**
     * Busca todos os refresh tokens.
     *
     * @return lista de todos os refresh tokens
     */
    List<RefreshToken> findAll();

    /**
     * Remove todos os refresh tokens do repositório.
     * <p>
     * Útil para testes.
     */
    void deleteAll();
}


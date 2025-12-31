package br.com.shooping.list.infrastructure.persistence.user;

import br.com.shooping.list.domain.user.RefreshToken;
import br.com.shooping.list.domain.user.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementação JPA do repositório de RefreshToken (Adapter - Clean Architecture).
 * <p>
 * Implementa o contrato definido em {@link RefreshTokenRepository} usando Spring Data JPA.
 * <p>
 * Esta é a camada de infraestrutura que depende dos detalhes técnicos (JPA),
 * mas implementa a interface definida no domínio (inversão de dependência - SOLID).
 */
@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

    /**
     * Busca um refresh token pelo hash.
     * <p>
     * Método herdado do contrato RefreshTokenRepository.
     */
    @Override
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}


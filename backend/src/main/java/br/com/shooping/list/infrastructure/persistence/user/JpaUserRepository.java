package br.com.shooping.list.infrastructure.persistence.user;

import br.com.shooping.list.domain.user.User;
import br.com.shooping.list.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementação JPA do repositório de User (Adapter - Clean Architecture).
 * <p>
 * Implementa o contrato definido em {@link UserRepository} usando Spring Data JPA.
 * <p>
 * Esta é a camada de infraestrutura que depende dos detalhes técnicos (JPA),
 * mas implementa a interface definida no domínio (inversão de dependência - SOLID).
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

    /**
     * Busca um usuário por email.
     * <p>
     * Método herdado do contrato UserRepository.
     */
    @Override
    Optional<User> findByEmail(String email);

    /**
     * Verifica se já existe um usuário com o email informado.
     * <p>
     * Método herdado do contrato UserRepository.
     */
    @Override
    boolean existsByEmail(String email);
}


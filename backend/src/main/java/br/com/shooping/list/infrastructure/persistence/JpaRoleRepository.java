package br.com.shooping.list.infrastructure.persistence;

import br.com.shooping.list.domain.user.Role;
import br.com.shooping.list.domain.user.RoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter JPA para acesso a roles no banco de dados.
 * Implementa o port RoleRepository definido no domínio.
 *
 * Spring Data JPA gera a implementação automaticamente baseado nos métodos declarados.
 */
@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long>, RoleRepository {

    /**
     * Busca role pelo nome.
     * Query derivada do nome do método (Spring Data JPA).
     *
     * @param name nome da role (case sensitive)
     * @return Optional contendo a role se encontrada
     */
    @Override
    Optional<Role> findByName(String name);

    /**
     * Verifica se existe role com o nome informado.
     * Query derivada do nome do método (Spring Data JPA).
     *
     * @param name nome da role
     * @return true se existe
     */
    @Override
    boolean existsByName(String name);
}


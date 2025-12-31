package br.com.shooping.list.domain.user;

import java.util.Optional;

/**
 * Port (interface) para acesso a roles no sistema.
 * Define o contrato que deve ser implementado pela camada de infraestrutura.
 *
 * Segue o padrão Repository do DDD e Hexagonal Architecture.
 */
public interface RoleRepository {

    /**
     * Busca uma role pelo nome.
     * Utilizado principalmente para buscar roles padrão como USER e ADMIN.
     *
     * @param name nome da role (ex: "USER", "ADMIN")
     * @return Optional contendo a role se encontrada, vazio caso contrário
     */
    Optional<Role> findByName(String name);

    /**
     * Salva ou atualiza uma role.
     *
     * @param role role a ser salva
     * @return role salva com ID gerado
     */
    Role save(Role role);

    /**
     * Busca uma role pelo ID.
     *
     * @param id identificador da role
     * @return Optional contendo a role se encontrada, vazio caso contrário
     */
    Optional<Role> findById(Long id);

    /**
     * Verifica se existe uma role com o nome informado.
     *
     * @param name nome da role
     * @return true se existe, false caso contrário
     */
    boolean existsByName(String name);
}


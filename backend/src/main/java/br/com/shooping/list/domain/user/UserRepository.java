package br.com.shooping.list.domain.user;

import java.util.Optional;

/**
 * Contrato de repositório para User (Port - Clean Architecture).
 * <p>
 * Define as operações de persistência necessárias para o agregado User,
 * sem depender de detalhes de infraestrutura (JPA, JDBC, etc).
 * <p>
 * A implementação concreta fica na camada infrastructure.
 */
public interface UserRepository {

    /**
     * Salva um usuário (insert ou update).
     *
     * @param user usuário a ser salvo
     * @return usuário salvo com ID preenchido
     */
    User save(User user);

    /**
     * Busca um usuário por email.
     *
     * @param email email do usuário
     * @return Optional com usuário se encontrado, empty caso contrário
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se já existe um usuário com o email informado.
     *
     * @param email email a ser verificado
     * @return true se existir, false caso contrário
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário por ID.
     *
     * @param id ID do usuário
     * @return Optional com usuário se encontrado, empty caso contrário
     */
    Optional<User> findById(Long id);

    /**
     * Remove todos os usuários do repositório.
     * <p>
     * Útil para testes.
     */
    void deleteAll();
}


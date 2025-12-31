package br.com.shooping.list.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidade Role representa um papel/função no sistema.
 * Define o que um usuário pode fazer (autorização).
 *
 * Regras de negócio:
 * - Nome da role é único no sistema
 * - Roles padrão são: USER (usuário comum) e ADMIN (administrador)
 * - Role é imutável após criação (apenas description pode ser alterada)
 */
@Entity
@Table(name = "tb_role")
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Construtor privado para uso interno.
     * Use factory methods para criar instâncias.
     */
    private Role(String name, String description) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method para criar uma nova role.
     *
     * @param name nome da role (ex: USER, ADMIN)
     * @param description descrição da role
     * @return nova instância de Role
     * @throws IllegalArgumentException se nome for inválido
     */
    public static Role create(String name, String description) {
        return new Role(name, description);
    }

    /**
     * Atualiza a descrição da role.
     * Nome não pode ser alterado pois é identificador único.
     *
     * @param description nova descrição
     */
    public void updateDescription(String description) {
        this.description = description;
        this.updatedAt = Instant.now();
    }

    /**
     * Valida o nome da role.
     *
     * @param name nome a ser validado
     * @throws IllegalArgumentException se nome for inválido
     */
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome da role não pode ser vazio");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Nome da role não pode ter mais de 50 caracteres");
        }
        // Convenção: roles em UPPERCASE
        if (!name.equals(name.toUpperCase())) {
            throw new IllegalArgumentException("Nome da role deve ser em UPPERCASE");
        }
    }

    /**
     * Retorna o nome da role com prefixo ROLE_ para Spring Security.
     * Ex: USER -> ROLE_USER
     *
     * @return nome da role com prefixo
     */
    public String getNameWithPrefix() {
        return "ROLE_" + this.name;
    }

    /**
     * Verifica se esta role é ADMIN.
     *
     * @return true se for ADMIN
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.name);
    }

    /**
     * Verifica se esta role é USER.
     *
     * @return true se for USER
     */
    public boolean isUser() {
        return "USER".equals(this.name);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}


package br.com.shooping.list.domain.shoppinglist;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

/**
 * Value Object que representa o nome de um item.
 * Garante que nomes sejam válidos e fornece normalização para comparação case-insensitive.
 * Regras:
 * - Nome não pode ser nulo ou vazio
 * - Nome deve ter entre 2 e 100 caracteres (após trim)
 * - Nome é normalizado para lowercase internamente para comparações
 */
@Embeddable
@Getter
public final class ItemName {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 100;

    /**
     * Retorna o valor original do nome (com capitalização preservada).
     */
    @Column(name = "name", nullable = false, length = 100)
    private String value;

    /**
     * Retorna o valor normalizado (lowercase, trimmed) para comparações.
     */
    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedValue;

    /**
     * Construtor protegido para JPA.
     */
    protected ItemName() {
    }

    private ItemName(String value) {
        this.value = validate(value);
        this.normalizedValue = this.value.toLowerCase().trim();
    }

    /**
     * Factory method para criar um nome de item.
     *
     * @param value nome do item
     * @return instância de ItemName
     * @throws IllegalArgumentException se nome for inválido
     */
    public static ItemName of(String value) {
        return new ItemName(value);
    }

    private String validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Nome do item não pode ser vazio");
        }

        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Nome do item deve ter no mínimo %d caracteres", MIN_LENGTH)
            );
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Nome do item deve ter no máximo %d caracteres", MAX_LENGTH)
            );
        }

        return trimmed;
    }

    /**
     * Verifica se este nome é igual a outro (case-insensitive).
     */
    public boolean isSameAs(ItemName other) {
        if (other == null) {
            return false;
        }
        return this.normalizedValue.equals(other.normalizedValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemName itemName = (ItemName) o;
        return Objects.equals(normalizedValue, itemName.normalizedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedValue);
    }

    @Override
    public String toString() {
        return value;
    }
}

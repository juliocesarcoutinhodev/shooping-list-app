package br.com.shooping.list.domain.shoppinglist;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa a quantidade de um item.
 * Garante que quantidades sejam sempre válidas (maior que zero).
 * Regras:
 * - Quantidade não pode ser nula
 * - Quantidade deve ser maior que zero
 * - Usa BigDecimal para precisão em números decimais
 */
@Getter
public final class Quantity {

    /**
     * — - GETTER --
     *  Retorna o valor da quantidade como BigDecimal.
     */
    private final BigDecimal value;

    private Quantity(BigDecimal value) {
        this.value = validate(value);
    }

    /**
     * Factory method para criar uma quantidade a partir de BigDecimal.
     *
     * @param value valor da quantidade
     * @return instância de Quantity
     * @throws IllegalArgumentException se quantidade for inválida
     */
    public static Quantity of(BigDecimal value) {
        return new Quantity(value);
    }

    /**
     * Factory method para criar uma quantidade a partir de double.
     * Conveniente para testes e casos simples.
     *
     * @param value valor da quantidade
     * @return instância de Quantity
     * @throws IllegalArgumentException se quantidade for inválida
     */
    public static Quantity of(double value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    /**
     * Factory method para criar uma quantidade a partir de int.
     * Conveniente para quantidades inteiras.
     *
     * @param value valor da quantidade
     * @return instância de Quantity
     * @throws IllegalArgumentException se quantidade for inválida
     */
    public static Quantity of(int value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    private BigDecimal validate(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Quantidade não pode ser nula");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        return value;
    }

    /**
     * Verifica se esta quantidade é maior que outra.
     */
    public boolean isGreaterThan(Quantity other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * Verifica se esta quantidade é menor que outra.
     */
    public boolean isLessThan(Quantity other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * Adiciona outra quantidade a esta.
     */
    public Quantity add(Quantity other) {
        return new Quantity(this.value.add(other.value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return value.compareTo(quantity.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

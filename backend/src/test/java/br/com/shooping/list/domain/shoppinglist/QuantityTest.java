package br.com.shooping.list.domain.shoppinglist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Quantity Value Object")
class QuantityTest {

    @Test
    @DisplayName("Deve criar quantidade válida a partir de BigDecimal")
    void shouldCreateValidQuantityFromBigDecimal() {
        Quantity quantity = Quantity.of(new BigDecimal("2.5"));

        assertThat(quantity.getValue()).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    @DisplayName("Deve criar quantidade válida a partir de double")
    void shouldCreateValidQuantityFromDouble() {
        Quantity quantity = Quantity.of(3.5);

        assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(3.5));
    }

    @Test
    @DisplayName("Deve criar quantidade válida a partir de int")
    void shouldCreateValidQuantityFromInt() {
        Quantity quantity = Quantity.of(5);

        assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for nula")
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> Quantity.of((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for zero")
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertThatThrownBy(() -> Quantity.of(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade for negativa")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThatThrownBy(() -> Quantity.of(-1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve aceitar quantidade decimal pequena")
    void shouldAcceptSmallDecimalQuantity() {
        Quantity quantity = Quantity.of(0.01);

        assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(0.01));
    }

    @Test
    @DisplayName("Deve aceitar quantidade muito grande")
    void shouldAcceptVeryLargeQuantity() {
        Quantity quantity = Quantity.of(999999.99);

        assertThat(quantity.getValue()).isEqualByComparingTo(BigDecimal.valueOf(999999.99));
    }

    @Test
    @DisplayName("Deve comparar se quantidade é maior que outra")
    void shouldCompareIfQuantityIsGreaterThan() {
        Quantity quantity1 = Quantity.of(5);
        Quantity quantity2 = Quantity.of(3);

        assertThat(quantity1.isGreaterThan(quantity2)).isTrue();
        assertThat(quantity2.isGreaterThan(quantity1)).isFalse();
    }

    @Test
    @DisplayName("Deve comparar se quantidade é menor que outra")
    void shouldCompareIfQuantityIsLessThan() {
        Quantity quantity1 = Quantity.of(3);
        Quantity quantity2 = Quantity.of(5);

        assertThat(quantity1.isLessThan(quantity2)).isTrue();
        assertThat(quantity2.isLessThan(quantity1)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false ao comparar quantidades iguais")
    void shouldReturnFalseWhenComparingEqualQuantities() {
        Quantity quantity1 = Quantity.of(5);
        Quantity quantity2 = Quantity.of(5);

        assertThat(quantity1.isGreaterThan(quantity2)).isFalse();
        assertThat(quantity1.isLessThan(quantity2)).isFalse();
    }

    @Test
    @DisplayName("Deve adicionar quantidades")
    void shouldAddQuantities() {
        Quantity quantity1 = Quantity.of(2.5);
        Quantity quantity2 = Quantity.of(1.5);

        Quantity result = quantity1.add(quantity2);

        assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
    }

    @Test
    @DisplayName("Deve ter equals baseado em comparação de valor")
    void shouldHaveEqualsBasedOnValueComparison() {
        Quantity quantity1 = Quantity.of(5.0);
        Quantity quantity2 = Quantity.of(5);
        Quantity quantity3 = Quantity.of(new BigDecimal("5.00"));

        assertThat(quantity1).isEqualTo(quantity2);
        assertThat(quantity1).isEqualTo(quantity3);
        assertThat(quantity2).isEqualTo(quantity3);
    }

    @Test
    @DisplayName("Deve retornar false no equals para quantidades diferentes")
    void shouldReturnFalseInEqualsForDifferentQuantities() {
        Quantity quantity1 = Quantity.of(5);
        Quantity quantity2 = Quantity.of(3);

        assertThat(quantity1).isNotEqualTo(quantity2);
    }

    @Test
    @DisplayName("Deve ter toString retornando valor como string")
    void shouldHaveToStringReturningValueAsString() {
        Quantity quantity = Quantity.of(2.5);

        assertThat(quantity.toString()).isEqualTo("2.5");
    }
}

package br.com.shooping.list.domain.shoppinglist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ItemName Value Object")
class ItemNameTest {

    @Test
    @DisplayName("Deve criar item name válido")
    void shouldCreateValidItemName() {
        ItemName name = ItemName.of("Arroz");

        assertThat(name.getValue()).isEqualTo("Arroz");
        assertThat(name.getNormalizedValue()).isEqualTo("arroz");
    }

    @Test
    @DisplayName("Deve fazer trim do valor ao criar")
    void shouldTrimValueOnCreation() {
        ItemName name = ItemName.of("  Feijão  ");

        assertThat(name.getValue()).isEqualTo("Feijão");
        assertThat(name.getNormalizedValue()).isEqualTo("feijão");
    }

    @Test
    @DisplayName("Deve normalizar para lowercase")
    void shouldNormalizeToLowercase() {
        ItemName name = ItemName.of("MACARRÃO");

        assertThat(name.getNormalizedValue()).isEqualTo("macarrão");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for nulo")
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> ItemName.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do item não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for vazio")
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> ItemName.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do item não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome for apenas espaços")
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> ItemName.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do item não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome tiver menos de 2 caracteres")
    void shouldThrowExceptionWhenNameIsTooShort() {
        assertThatThrownBy(() -> ItemName.of("A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do item deve ter no mínimo 2 caracteres");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome tiver mais de 100 caracteres")
    void shouldThrowExceptionWhenNameIsTooLong() {
        String longName = "A".repeat(101);

        assertThatThrownBy(() -> ItemName.of(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome do item deve ter no máximo 100 caracteres");
    }

    @Test
    @DisplayName("Deve aceitar nome com exatamente 2 caracteres")
    void shouldAcceptNameWithExactlyTwoCharacters() {
        ItemName name = ItemName.of("Ab");

        assertThat(name.getValue()).isEqualTo("Ab");
    }

    @Test
    @DisplayName("Deve aceitar nome com exatamente 100 caracteres")
    void shouldAcceptNameWithExactly100Characters() {
        String maxLengthName = "A".repeat(100);
        ItemName name = ItemName.of(maxLengthName);

        assertThat(name.getValue()).hasSize(100);
    }

    @Test
    @DisplayName("Deve comparar nomes ignorando case")
    void shouldCompareNamesIgnoringCase() {
        ItemName name1 = ItemName.of("Arroz");
        ItemName name2 = ItemName.of("ARROZ");
        ItemName name3 = ItemName.of("arroz");

        assertThat(name1.isSameAs(name2)).isTrue();
        assertThat(name1.isSameAs(name3)).isTrue();
        assertThat(name2.isSameAs(name3)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false ao comparar nomes diferentes")
    void shouldReturnFalseWhenComparingDifferentNames() {
        ItemName name1 = ItemName.of("Arroz");
        ItemName name2 = ItemName.of("Feijão");

        assertThat(name1.isSameAs(name2)).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false ao comparar com null")
    void shouldReturnFalseWhenComparingWithNull() {
        ItemName name = ItemName.of("Arroz");

        assertThat(name.isSameAs(null)).isFalse();
    }

    @Test
    @DisplayName("Deve ter equals baseado em valor normalizado")
    void shouldHaveEqualsBasedOnNormalizedValue() {
        ItemName name1 = ItemName.of("Arroz");
        ItemName name2 = ItemName.of("ARROZ");

        assertThat(name1).isEqualTo(name2);
    }

    @Test
    @DisplayName("Deve ter hashCode baseado em valor normalizado")
    void shouldHaveHashCodeBasedOnNormalizedValue() {
        ItemName name1 = ItemName.of("Arroz");
        ItemName name2 = ItemName.of("ARROZ");

        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString retornando valor original")
    void shouldHaveToStringReturningOriginalValue() {
        ItemName name = ItemName.of("Arroz Integral");

        assertThat(name.toString()).isEqualTo("Arroz Integral");
    }
}

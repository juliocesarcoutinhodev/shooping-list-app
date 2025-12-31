package br.com.shooping.list.domain.shoppinglist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ListItem Entity")
class ListItemTest {

    private ShoppingList shoppingList;

    @BeforeEach
    void setUp() {
        shoppingList = ShoppingList.create(1L, "Mercado", "Lista do mercado");
    }

    @Test
    @DisplayName("Deve criar item válido")
    void shouldCreateValidItem() {
        ItemName name = ItemName.of("Arroz");
        Quantity quantity = Quantity.of(2);

        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getQuantityAsValueObject()).isEqualTo(quantity);
        assertThat(item.getUnit()).isEqualTo("kg");
        assertThat(item.getStatus()).isEqualTo(ItemStatus.PENDING);
        assertThat(item.getShoppingList()).isEqualTo(shoppingList);
        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar item sem unidade de medida")
    void shouldCreateItemWithoutUnit() {
        ItemName name = ItemName.of("Pão");
        Quantity quantity = Quantity.of(5);

        ListItem item = ListItem.create(shoppingList, name, quantity, null, null);

        assertThat(item.getUnit()).isNull();
    }

    @Test
    @DisplayName("Deve fazer trim da unidade ao criar")
    void shouldTrimUnitOnCreation() {
        ItemName name = ItemName.of("Leite");
        Quantity quantity = Quantity.of(1);

        ListItem item = ListItem.create(shoppingList, name, quantity, "  litro  ", null);

        assertThat(item.getUnit()).isEqualTo("litro");
    }

    @Test
    @DisplayName("Deve converter unidade vazia em null")
    void shouldConvertEmptyUnitToNull() {
        ItemName name = ItemName.of("Banana");
        Quantity quantity = Quantity.of(6);

        ListItem item = ListItem.create(shoppingList, name, quantity, "   ", null);

        assertThat(item.getUnit()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando lista for nula")
    void shouldThrowExceptionWhenShoppingListIsNull() {
        ItemName name = ItemName.of("Arroz");
        Quantity quantity = Quantity.of(2);

        assertThatThrownBy(() -> ListItem.create(null, name, quantity, "kg", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lista de compras não pode ser nula");
    }

    @Test
    @DisplayName("Deve lançar exceção quando unidade tiver mais de 20 caracteres")
    void shouldThrowExceptionWhenUnitIsTooLong() {
        ItemName name = ItemName.of("Produto");
        Quantity quantity = Quantity.of(1);
        String longUnit = "A".repeat(21);

        assertThatThrownBy(() -> ListItem.create(shoppingList, name, quantity, longUnit, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unidade de medida não pode ter mais de 20 caracteres");
    }

    @Test
    @DisplayName("Deve marcar item como comprado")
    void shouldMarkItemAsPurchased() {
        ItemName name = ItemName.of("Café");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "pacote", null);

        item.markAsPurchased();

        assertThat(item.getStatus()).isEqualTo(ItemStatus.PURCHASED);
        assertThat(item.isPurchased()).isTrue();
        assertThat(item.isPending()).isFalse();
    }

    @Test
    @DisplayName("Deve marcar item como pendente")
    void shouldMarkItemAsPending() {
        ItemName name = ItemName.of("Açúcar");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);
        item.markAsPurchased();

        item.markAsPending();

        assertThat(item.getStatus()).isEqualTo(ItemStatus.PENDING);
        assertThat(item.isPending()).isTrue();
        assertThat(item.isPurchased()).isFalse();
    }

    @Test
    @DisplayName("Não deve fazer nada ao marcar item já comprado como comprado")
    void shouldDoNothingWhenMarkingAlreadyPurchasedItemAsPurchased() {
        ItemName name = ItemName.of("Óleo");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "litro", null);
        item.markAsPurchased();

        item.markAsPurchased();

        assertThat(item.isPurchased()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar quantidade do item")
    void shouldUpdateItemQuantity() {
        ItemName name = ItemName.of("Maçã");
        Quantity quantity = Quantity.of(3);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        Quantity newQuantity = Quantity.of(5);
        item.updateQuantity(newQuantity);

        assertThat(item.getQuantityAsValueObject()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar quantidade para null")
    void shouldThrowExceptionWhenUpdatingQuantityToNull() {
        ItemName name = ItemName.of("Tomate");
        Quantity quantity = Quantity.of(2);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        assertThatThrownBy(() -> item.updateQuantity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade não pode ser nula");
    }

    @Test
    @DisplayName("Deve atualizar nome do item")
    void shouldUpdateItemName() {
        ItemName name = ItemName.of("Feijão Preto");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        ItemName newName = ItemName.of("Feijão Carioca");
        item.updateName(newName);

        assertThat(item.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar nome para null")
    void shouldThrowExceptionWhenUpdatingNameToNull() {
        ItemName name = ItemName.of("Sal");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        assertThatThrownBy(() -> item.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome não pode ser nulo");
    }

    @Test
    @DisplayName("Deve atualizar unidade do item")
    void shouldUpdateItemUnit() {
        ItemName name = ItemName.of("Refrigerante");
        Quantity quantity = Quantity.of(2);
        ListItem item = ListItem.create(shoppingList, name, quantity, "litro", null);

        item.updateUnit("garrafa");

        assertThat(item.getUnit()).isEqualTo("garrafa");
    }

    @Test
    @DisplayName("Deve permitir atualizar unidade para null")
    void shouldAllowUpdatingUnitToNull() {
        ItemName name = ItemName.of("Ovos");
        Quantity quantity = Quantity.of(12);
        ListItem item = ListItem.create(shoppingList, name, quantity, "unidades", null);

        item.updateUnit(null);

        assertThat(item.getUnit()).isNull();
    }

    @Test
    @DisplayName("Deve verificar se item tem mesmo nome que outro item")
    void shouldCheckIfItemHasSameNameAsAnotherItem() {
        ItemName name1 = ItemName.of("Arroz");
        ItemName name2 = ItemName.of("ARROZ");
        Quantity quantity = Quantity.of(1);

        ListItem item1 = ListItem.create(shoppingList, name1, quantity, "kg", null);
        ListItem item2 = ListItem.create(shoppingList, name2, quantity, "kg", null);

        assertThat(item1.hasSameNameAs(item2)).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se item tem nome específico")
    void shouldCheckIfItemHasSpecificName() {
        ItemName name = ItemName.of("Macarrão");
        Quantity quantity = Quantity.of(2);
        ListItem item = ListItem.create(shoppingList, name, quantity, "pacote", null);

        ItemName searchName = ItemName.of("MACARRÃO");

        assertThat(item.hasName(searchName)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false ao verificar nome com null")
    void shouldReturnFalseWhenCheckingNameWithNull() {
        ItemName name = ItemName.of("Farinha");
        Quantity quantity = Quantity.of(1);
        ListItem item = ListItem.create(shoppingList, name, quantity, "kg", null);

        assertThat(item.hasSameNameAs(null)).isFalse();
    }

    @Test
    @DisplayName("Deve ter equals baseado em ID")
    void shouldHaveEqualsBasedOnId() {
        ItemName name = ItemName.of("Presunto");
        Quantity quantity = Quantity.of(200);
        ListItem item1 = ListItem.create(shoppingList, name, quantity, "gramas", null);
        ListItem item2 = ListItem.create(shoppingList, name, quantity, "gramas", null);

        item1.setId(1L);
        item2.setId(1L);

        assertThat(item1).isEqualTo(item2);
    }

    @Test
    @DisplayName("Deve ter toString contendo informações principais")
    void shouldHaveToStringWithMainInfo() {
        ItemName name = ItemName.of("Queijo");
        Quantity quantity = Quantity.of(300);
        ListItem item = ListItem.create(shoppingList, name, quantity, "gramas", null);
        item.setId(5L);

        String toString = item.toString();

        assertThat(toString).contains("5", "Queijo", "300", "gramas", "PENDING");
    }
}

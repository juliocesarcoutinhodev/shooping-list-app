package br.com.shooping.list.domain.shoppinglist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("ShoppingList Aggregate Root")
class ShoppingListTest {

    @Test
    @DisplayName("Deve criar lista válida")
    void shouldCreateValidList() {
        ShoppingList list = ShoppingList.create(1L, "Mercado Mensal", "Lista do mercado do mês");

        assertThat(list.getOwnerId()).isEqualTo(1L);
        assertThat(list.getTitle()).isEqualTo("Mercado Mensal");
        assertThat(list.getDescription()).isEqualTo("Lista do mercado do mês");
        assertThat(list.getItems()).isEmpty();
        assertThat(list.getCreatedAt()).isNotNull();
        assertThat(list.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar lista sem descrição")
    void shouldCreateListWithoutDescription() {
        ShoppingList list = ShoppingList.create(1L, "Compras", null);

        assertThat(list.getDescription()).isNull();
    }

    @Test
    @DisplayName("Deve fazer trim do título ao criar")
    void shouldTrimTitleOnCreation() {
        ShoppingList list = ShoppingList.create(1L, "  Lista Semanal  ", null);

        assertThat(list.getTitle()).isEqualTo("Lista Semanal");
    }

    @Test
    @DisplayName("Deve fazer trim da descrição ao criar")
    void shouldTrimDescriptionOnCreation() {
        ShoppingList list = ShoppingList.create(1L, "Compras", "  Descrição  ");

        assertThat(list.getDescription()).isEqualTo("Descrição");
    }

    @Test
    @DisplayName("Deve converter descrição vazia em null")
    void shouldConvertEmptyDescriptionToNull() {
        ShoppingList list = ShoppingList.create(1L, "Compras", "   ");

        assertThat(list.getDescription()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando ownerId for nulo")
    void shouldThrowExceptionWhenOwnerIdIsNull() {
        assertThatThrownBy(() -> ShoppingList.create(null, "Lista", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do dono da lista não pode ser nulo");
    }

    @Test
    @DisplayName("Deve lançar exceção quando título for nulo")
    void shouldThrowExceptionWhenTitleIsNull() {
        assertThatThrownBy(() -> ShoppingList.create(1L, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Título da lista não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando título for vazio")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertThatThrownBy(() -> ShoppingList.create(1L, "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Título da lista não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando título for apenas espaços")
    void shouldThrowExceptionWhenTitleIsBlank() {
        assertThatThrownBy(() -> ShoppingList.create(1L, "   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Título da lista não pode ser vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando título tiver menos de 3 caracteres")
    void shouldThrowExceptionWhenTitleIsTooShort() {
        assertThatThrownBy(() -> ShoppingList.create(1L, "Ab", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Título da lista deve ter no mínimo 3 caracteres");
    }

    @Test
    @DisplayName("Deve lançar exceção quando título tiver mais de 100 caracteres")
    void shouldThrowExceptionWhenTitleIsTooLong() {
        String longTitle = "A".repeat(101);

        assertThatThrownBy(() -> ShoppingList.create(1L, longTitle, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Título da lista deve ter no máximo 100 caracteres");
    }

    @Test
    @DisplayName("Deve aceitar título com exatamente 3 caracteres")
    void shouldAcceptTitleWithExactlyThreeCharacters() {
        ShoppingList list = ShoppingList.create(1L, "ABC", null);

        assertThat(list.getTitle()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("Deve aceitar título com exatamente 100 caracteres")
    void shouldAcceptTitleWithExactly100Characters() {
        String maxTitle = "A".repeat(100);
        ShoppingList list = ShoppingList.create(1L, maxTitle, null);

        assertThat(list.getTitle()).hasSize(100);
    }

    @Test
    @DisplayName("Deve lançar exceção quando descrição tiver mais de 255 caracteres")
    void shouldThrowExceptionWhenDescriptionIsTooLong() {
        String longDescription = "A".repeat(256);

        assertThatThrownBy(() -> ShoppingList.create(1L, "Lista", longDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Descrição da lista deve ter no máximo 255 caracteres");
    }

    @Test
    @DisplayName("Deve atualizar título da lista")
    void shouldUpdateListTitle() {
        ShoppingList list = ShoppingList.create(1L, "Título Antigo", null);

        list.updateTitle("Novo Título");

        assertThat(list.getTitle()).isEqualTo("Novo Título");
    }

    @Test
    @DisplayName("Deve atualizar descrição da lista")
    void shouldUpdateListDescription() {
        ShoppingList list = ShoppingList.create(1L, "Lista", "Descrição antiga");

        list.updateDescription("Nova descrição");

        assertThat(list.getDescription()).isEqualTo("Nova descrição");
    }

    @Test
    @DisplayName("Deve adicionar item à lista")
    void shouldAddItemToList() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ItemName name = ItemName.of("Arroz");
        Quantity quantity = Quantity.of(2);

        ListItem item = list.addItem(name, quantity, "kg", null);

        assertThat(list.getItems()).hasSize(1);
        assertThat(list.countTotalItems()).isEqualTo(1);
        assertThat(item.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Deve adicionar múltiplos itens à lista")
    void shouldAddMultipleItemsToList() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);

        list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);
        list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", null);
        list.addItem(ItemName.of("Macarrão"), Quantity.of(3), "pacote", null);

        assertThat(list.countTotalItems()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar item duplicado")
    void shouldThrowExceptionWhenAddingDuplicateItem() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);

        assertThatThrownBy(() -> list.addItem(ItemName.of("ARROZ"), Quantity.of(1), "kg", BigDecimal.valueOf(35.00)))
                .isInstanceOf(DuplicateItemException.class)
                .hasMessageContaining("Item 'ARROZ' já existe nesta lista");
    }

    @Test
    @DisplayName("Deve lançar exceção ao atingir limite de 100 itens")
    void shouldThrowExceptionWhenReachingItemLimit() {
        ShoppingList list = ShoppingList.create(1L, "Lista Grande", null);

        // Adiciona 100 itens (limite máximo)
        for (int i = 1; i <= 100; i++) {
            list.addItem(ItemName.of("Item " + i), Quantity.of(1), null, null);
        }

        assertThat(list.countTotalItems()).isEqualTo(100);

        // Tentar adicionar o 101º item deve lançar exceção
        assertThatThrownBy(() -> list.addItem(ItemName.of("Item 101"), Quantity.of(1), null, null))
                .isInstanceOf(ListLimitExceededException.class)
                .hasMessageContaining("Lista não pode ter mais de 100 itens");
    }

    @Test
    @DisplayName("Deve remover item da lista")
    void shouldRemoveItemFromList() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);
        item.setId(1L);

        list.removeItem(1L);

        assertThat(list.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover item inexistente")
    void shouldThrowExceptionWhenRemovingNonExistentItem() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);

        assertThatThrownBy(() -> list.removeItem(999L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Item com ID 999 não encontrado nesta lista");
    }

    @Test
    @DisplayName("Deve marcar item como comprado")
    void shouldMarkItemAsPurchased() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Café"), Quantity.of(1), "pacote", null);
        item.setId(1L);

        list.markItemAsPurchased(1L);

        assertThat(item.isPurchased()).isTrue();
        assertThat(list.countPurchasedItems()).isEqualTo(1);
        assertThat(list.countPendingItems()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve marcar item como pendente")
    void shouldMarkItemAsPending() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Açúcar"), Quantity.of(1), "kg", null);
        item.setId(1L);
        list.markItemAsPurchased(1L);

        list.markItemAsPending(1L);

        assertThat(item.isPending()).isTrue();
        assertThat(list.countPendingItems()).isEqualTo(1);
        assertThat(list.countPurchasedItems()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve limpar itens comprados")
    void shouldClearPurchasedItems() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item1 = list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", BigDecimal.valueOf(35.00));
        ListItem item2 = list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", BigDecimal.valueOf(20.00));
        ListItem item3 = list.addItem(ItemName.of("Macarrão"), Quantity.of(3), "pacote", BigDecimal.valueOf(5.00));
        item1.setId(1L);
        item2.setId(2L);
        item3.setId(3L);

        list.markItemAsPurchased(1L);
        list.markItemAsPurchased(3L);

        int removed = list.clearPurchasedItems();

        assertThat(removed).isEqualTo(2);
        assertThat(list.countTotalItems()).isEqualTo(1);
        assertThat(list.countPurchasedItems()).isEqualTo(0);
        assertThat(list.countPendingItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar zero ao limpar lista sem itens comprados")
    void shouldReturnZeroWhenClearingListWithoutPurchasedItems() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);
        list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", null);

        int removed = list.clearPurchasedItems();

        assertThat(removed).isEqualTo(0);
        assertThat(list.countTotalItems()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve atualizar quantidade do item")
    void shouldUpdateItemQuantity() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Leite"), Quantity.of(1), "litro", null);
        item.setId(1L);

        list.updateItemQuantity(1L, Quantity.of(3));

        assertThat(item.getQuantityAsValueObject()).isEqualTo(Quantity.of(3));
    }

    @Test
    @DisplayName("Deve atualizar nome do item")
    void shouldUpdateItemName() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Feijão Preto"), Quantity.of(1), "kg", null);
        item.setId(1L);

        list.updateItemName(1L, ItemName.of("Feijão Carioca"));

        assertThat(item.getName().getValue()).isEqualTo("Feijão Carioca");
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar nome para nome duplicado")
    void shouldThrowExceptionWhenUpdatingNameToDuplicateName() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item1 = list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", BigDecimal.valueOf(35.00));
        ListItem item2 = list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", BigDecimal.valueOf(20.00));
        item1.setId(1L);
        item2.setId(2L);

        assertThatThrownBy(() -> list.updateItemName(2L, ItemName.of("ARROZ")))
                .isInstanceOf(DuplicateItemException.class)
                .hasMessageContaining("Item 'ARROZ' já existe nesta lista");
    }

    @Test
    @DisplayName("Deve permitir atualizar item para o mesmo nome")
    void shouldAllowUpdatingItemToSameName() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Arroz Branco"), Quantity.of(2), "kg", null);
        item.setId(1L);

        // Atualizar para o mesmo nome (mas com capitalização diferente) não deve lançar exceção
        assertDoesNotThrow(() -> list.updateItemName(1L, ItemName.of("ARROZ BRANCO")));
    }

    @Test
    @DisplayName("Deve atualizar unidade do item")
    void shouldUpdateItemUnit() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Refrigerante"), Quantity.of(2), "litro", null);
        item.setId(1L);

        list.updateItemUnit(1L, "garrafa");

        assertThat(item.getUnit()).isEqualTo("garrafa");
    }

    @Test
    @DisplayName("Deve buscar item por ID")
    void shouldFindItemById() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item = list.addItem(ItemName.of("Sal"), Quantity.of(1), "kg", null);
        item.setId(5L);

        ListItem found = list.findItemById(5L);

        assertThat(found).isEqualTo(item);
    }

    @Test
    @DisplayName("Deve contar itens por status")
    void shouldCountItemsByStatus() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        ListItem item1 = list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", BigDecimal.valueOf(35.00));
        ListItem item2 = list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", BigDecimal.valueOf(20.00));
        ListItem item3 = list.addItem(ItemName.of("Macarrão"), Quantity.of(3), "pacote", BigDecimal.valueOf(5.00));
        ListItem item4 = list.addItem(ItemName.of("Óleo"), Quantity.of(1), "litro", null);
        item1.setId(1L);
        item2.setId(2L);
        item3.setId(3L);
        item4.setId(4L);

        list.markItemAsPurchased(1L);
        list.markItemAsPurchased(3L);

        assertThat(list.countTotalItems()).isEqualTo(4);
        assertThat(list.countPurchasedItems()).isEqualTo(2);
        assertThat(list.countPendingItems()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve retornar cópia imutável dos itens")
    void shouldReturnUnmodifiableCopyOfItems() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);
        list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);

        assertThatThrownBy(() -> list.getItems().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Deve verificar se usuário é dono da lista")
    void shouldCheckIfUserIsOwner() {
        ShoppingList list = ShoppingList.create(1L, "Mercado", null);

        assertThat(list.isOwnedBy(1L)).isTrue();
        assertThat(list.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("Deve ter equals baseado em ID")
    void shouldHaveEqualsBasedOnId() {
        ShoppingList list1 = ShoppingList.create(1L, "Lista 1", null);
        ShoppingList list2 = ShoppingList.create(2L, "Lista 2", null);

        list1.setId(10L);
        list2.setId(10L);

        assertThat(list1).isEqualTo(list2);
    }

    @Test
    @DisplayName("Deve ter toString contendo informações principais")
    void shouldHaveToStringWithMainInfo() {
        ShoppingList list = ShoppingList.create(5L, "Mercado Mensal", null);
        list.setId(15L);
        list.addItem(ItemName.of("Arroz"), Quantity.of(2), "kg", null);
        list.addItem(ItemName.of("Feijão"), Quantity.of(1), "kg", null);

        String toString = list.toString();

        assertThat(toString).contains("15", "5", "Mercado Mensal", "2");
    }
}

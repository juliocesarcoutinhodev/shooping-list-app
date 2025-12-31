package br.com.shooping.list.domain.shoppinglist;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root que representa uma lista de compras.
 * Gerencia o ciclo de vida dos itens e garante invariantes do aggregate.
 * Regras de negócio:
 * - Título da lista é obrigatório (3-100 caracteres)
 * - Descrição é opcional (até 255 caracteres)
 * - Dono da lista é obrigatório (ownerId)
 * - Não é permitido adicionar itens com nomes duplicados (case-insensitive)
 * - Lista pode ter no máximo 100 itens
 * - Apenas o dono da lista pode modificá-la
 * O ShoppingList é responsável por:
 * - Adicionar itens validando duplicatas e limite
 * - Remover itens
 * - Marcar itens como comprados/não comprados
 * - Limpar itens comprados
 * - Contar itens por status
 */
@Entity
@Table(name = "tb_shopping_list")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingList {

    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    private static final int MAX_ITEMS = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Construtor privado.
     * Use o factory method create() para criar instâncias.
     */
    private ShoppingList(Long ownerId, String title, String description) {
        validateOwnerId(ownerId);
        this.ownerId = ownerId;
        this.title = validateTitle(title);
        this.description = validateDescription(description);
        this.items = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method para criar uma nova lista de compras.
     *
     * @param ownerId ID do dono da lista (obrigatório)
     * @param title título da lista (obrigatório, 3-100 caracteres)
     * @param description descrição da lista (opcional, até 255 caracteres)
     * @return nova instância de ShoppingList
     * @throws IllegalArgumentException se parâmetros forem inválidos
     */
    public static ShoppingList create(Long ownerId, String title, String description) {
        return new ShoppingList(ownerId, title, description);
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ID do dono da lista não pode ser nulo");
        }
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título da lista não pode ser vazio");
        }

        String trimmed = title.trim();
        if (trimmed.length() < MIN_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Título da lista deve ter no mínimo %d caracteres", MIN_TITLE_LENGTH)
            );
        }

        if (trimmed.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Título da lista deve ter no máximo %d caracteres", MAX_TITLE_LENGTH)
            );
        }

        return trimmed;
    }

    private String validateDescription(String description) {
        if (description == null) {
            return null;
        }

        String trimmed = description.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Descrição da lista deve ter no máximo %d caracteres", MAX_DESCRIPTION_LENGTH)
            );
        }

        return trimmed;
    }

    /**
     * Atualiza o título da lista.
     *
     * @param title novo título (3-100 caracteres)
     * @throws IllegalArgumentException se título for inválido
     */
    public void updateTitle(String title) {
        this.title = validateTitle(title);
        this.updatedAt = Instant.now();
    }

    /**
     * Atualiza a descrição da lista.
     *
     * @param description nova descrição (opcional, até 255 caracteres)
     * @throws IllegalArgumentException se descrição for inválida
     */
    public void updateDescription(String description) {
        this.description = validateDescription(description);
        this.updatedAt = Instant.now();
    }

    /**
     * Adiciona um novo item à lista.
     * Valida duplicatas (por nome normalizado) e limite de itens.
     *
     * @param name nome do item (obrigatório)
     * @param quantity quantidade (obrigatório, maior que zero)
     * @param unit unidade de medida (opcional)
     * @param unitPrice preço unitário (opcional)
     * @return item criado
     * @throws DuplicateItemException se já existe item com mesmo nome
     * @throws ListLimitExceededException se lista atingiu limite de 100 itens
     */
    public ListItem addItem(ItemName name, Quantity quantity, String unit, java.math.BigDecimal unitPrice) {
        validateItemLimit();
        validateDuplicateItem(name);

        ListItem item = ListItem.create(this, name, quantity, unit, unitPrice);
        items.add(item);
        this.updatedAt = Instant.now();
        return item;
    }

    private void validateItemLimit() {
        if (items.size() >= MAX_ITEMS) {
            throw new ListLimitExceededException(MAX_ITEMS);
        }
    }

    private void validateDuplicateItem(ItemName name) {
        boolean isDuplicate = items.stream()
                .anyMatch(item -> item.hasName(name));

        if (isDuplicate) {
            throw new DuplicateItemException(name.getValue());
        }
    }

    /**
     * Remove um item da lista.
     *
     * @param itemId ID do item a ser removido
     * @throws ItemNotFoundException se item não existir na lista
     */
    public void removeItem(Long itemId) {
        ListItem item = findItemById(itemId);
        items.remove(item);
        this.updatedAt = Instant.now();
    }

    /**
     * Remove todos os itens marcados como comprados.
     *
     * @return quantidade de itens removidos
     */
    public int clearPurchasedItems() {
        List<ListItem> purchasedItems = items.stream()
                .filter(ListItem::isPurchased)
                .toList();

        int count = purchasedItems.size();
        items.removeAll(purchasedItems);

        if (count > 0) {
            this.updatedAt = Instant.now();
        }

        return count;
    }

    /**
     * Marca um item como comprado.
     *
     * @param itemId ID do item
     * @throws ItemNotFoundException se item não existir na lista
     */
    public void markItemAsPurchased(Long itemId) {
        ListItem item = findItemById(itemId);
        item.markAsPurchased();
        this.updatedAt = Instant.now();
    }

    /**
     * Marca um item como não comprado (pending).
     *
     * @param itemId ID do item
     * @throws ItemNotFoundException se item não existir na lista
     */
    public void markItemAsPending(Long itemId) {
        ListItem item = findItemById(itemId);
        item.markAsPending();
        this.updatedAt = Instant.now();
    }

    /**
     * Atualiza a quantidade de um item.
     *
     * @param itemId ID do item
     * @param quantity nova quantidade
     * @throws ItemNotFoundException se item não existir na lista
     */
    public void updateItemQuantity(Long itemId, Quantity quantity) {
        ListItem item = findItemById(itemId);
        item.updateQuantity(quantity);
        this.updatedAt = Instant.now();
    }

    /**
     * Atualiza o nome de um item.
     * Valida se o novo nome não conflita com outros itens.
     *
     * @param itemId ID do item
     * @param name novo nome
     * @throws ItemNotFoundException se item não existir na lista
     * @throws DuplicateItemException se novo nome já existe em outro item
     */
    public void updateItemName(Long itemId, ItemName name) {
        ListItem item = findItemById(itemId);

        // Valida duplicata apenas com outros itens (não com ele mesmo)
        boolean isDuplicate = items.stream()
                .filter(i -> !i.equals(item))
                .anyMatch(i -> i.hasName(name));

        if (isDuplicate) {
            throw new DuplicateItemException(name.getValue());
        }

        item.updateName(name);
        this.updatedAt = Instant.now();
    }

    /**
     * Atualiza a unidade de medida de um item.
     *
     * @param itemId ID do item
     * @param unit nova unidade
     * @throws ItemNotFoundException se item não existir na lista
     */
    public void updateItemUnit(Long itemId, String unit) {
        ListItem item = findItemById(itemId);
        item.updateUnit(unit);
        this.updatedAt = Instant.now();
    }

    /**
     * Atualiza o preço unitário de um item.
     *
     * @param itemId ID do item
     * @param unitPrice novo preço unitário (pode ser null)
     * @throws ItemNotFoundException se item não existir na lista
     * @throws IllegalArgumentException se preço for negativo
     */
    public void updateItemUnitPrice(Long itemId, java.math.BigDecimal unitPrice) {
        ListItem item = findItemById(itemId);
        item.updateUnitPrice(unitPrice);
        this.updatedAt = Instant.now();
    }

    /**
     * Busca um item pelo ID.
     *
     * @param itemId ID do item
     * @return item encontrado
     * @throws ItemNotFoundException se item não existir na lista
     */
    public ListItem findItemById(Long itemId) {
        return items.stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    /**
     * Retorna o total de itens na lista.
     */
    public int countTotalItems() {
        return items.size();
    }

    /**
     * Retorna o total de itens não comprados.
     */
    public int countPendingItems() {
        return (int) items.stream()
                .filter(ListItem::isPending)
                .count();
    }

    /**
     * Retorna o total de itens comprados.
     */
    public int countPurchasedItems() {
        return (int) items.stream()
                .filter(ListItem::isPurchased)
                .count();
    }

    /**
     * Retorna uma cópia imutável dos itens para leitura.
     * Não permite modificação direta da coleção.
     */
    public List<ListItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Verifica se o usuário é o dono da lista.
     */
    public boolean isOwnedBy(Long userId) {
        return Objects.equals(this.ownerId, userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingList that = (ShoppingList) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ShoppingList{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", title='" + title + '\'' +
                ", itemsCount=" + items.size() +
                '}';
    }
}

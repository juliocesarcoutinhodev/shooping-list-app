package br.com.shooping.list.application.mapper;

import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.domain.shoppinglist.ListItem;
import br.com.shooping.list.domain.shoppinglist.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper MapStruct para conversão entre ShoppingList (domínio) e DTOs de resposta.
 * <p>
 * Responsabilidades:
 * - Mapear ShoppingList → ShoppingListResponse (completo, com itens)
 * - Mapear ShoppingList → ShoppingListResponse (sem itens)
 * - Mapear ShoppingList → ShoppingListSummaryResponse (resumo)
 * - Mapear ListItem → ItemResponse (inline)
 * <p>
 * Configuração:
 * - componentModel = "spring" - gera bean Spring injetável
 */
@Mapper(componentModel = "spring")
public interface ShoppingListMapper {

    /**
     * Mapeia ShoppingList para ShoppingListResponse COMPLETO (com itens).
     * Usado em: GetShoppingListByIdUseCase (retorna lista detalhada).
     * <p>
     * Mapeamentos:
     * - items: mapeado automaticamente via toItemResponse()
     * - itemsCount: chama método de domínio countTotalItems()
     * - pendingItemsCount: chama método de domínio countPendingItems()
     * - purchasedItemsCount: chama método de domínio countPurchasedItems()
     *
     * @param list entidade de domínio
     * @return DTO completo com todos os itens
     */
    @Mapping(target = "items", source = "items")
    @Mapping(target = "itemsCount", expression = "java(list.countTotalItems())")
    @Mapping(target = "pendingItemsCount", expression = "java(list.countPendingItems())")
    @Mapping(target = "purchasedItemsCount", expression = "java(list.countPurchasedItems())")
    ShoppingListResponse toResponse(ShoppingList list);

    /**
     * Mapeia ShoppingList para ShoppingListResponse SEM os itens.
     * Usado em: CreateShoppingListUseCase, UpdateShoppingListUseCase
     * (quando não é necessário retornar os itens, otimiza payload).
     * <p>
     * Diferença para toResponse():
     * - items = null (ignored)
     * - Demais campos iguais
     *
     * @param list entidade de domínio
     * @return DTO sem lista de itens (items = null)
     */
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "itemsCount", expression = "java(list.countTotalItems())")
    @Mapping(target = "pendingItemsCount", expression = "java(list.countPendingItems())")
    @Mapping(target = "purchasedItemsCount", expression = "java(list.countPurchasedItems())")
    ShoppingListResponse toResponseWithoutItems(ShoppingList list);

    /**
     * Mapeia ShoppingList para ShoppingListSummaryResponse (resumo).
     * Usado em: GetMyShoppingListsUseCase (listagem resumida).
     * <p>
     * DTO mais leve, contém apenas:
     * - id, title, itemsCount, pendingItemsCount, createdAt, updatedAt
     *
     * @param list entidade de domínio
     * @return DTO resumido
     */
    @Mapping(target = "itemsCount", expression = "java(list.countTotalItems())")
    @Mapping(target = "pendingItemsCount", expression = "java(list.countPendingItems())")
    ShoppingListSummaryResponse toSummaryResponse(ShoppingList list);

    /**
     * Mapeia lista de ShoppingLists para lista de ShoppingListSummaryResponses.
     * Útil para retornar múltiplas listas resumidas.
     *
     * @param lists lista de entidades de domínio
     * @return lista de DTOs resumidos
     */
    List<ShoppingListSummaryResponse> toSummaryResponseList(List<ShoppingList> lists);

    /**
     * Mapeia um ListItem de domínio para ItemResponse DTO.
     * Usado internamente pelo MapStruct ao mapear lista de itens.
     * <p>
     * Mapeamentos customizados:
     * - name: extrai valor do Value Object ItemName
     * - status: converte enum ItemStatus para String
     *
     * @param item entidade de domínio
     * @return DTO de resposta
     */
    @Mapping(target = "name", expression = "java(item.getName().getValue())")
    @Mapping(target = "status", expression = "java(item.getStatus().name())")
    ItemResponse toItemResponse(ListItem item);

    /**
     * Mapeia uma lista de ListItems para lista de ItemResponses.
     * Útil para mapear todos os itens de uma ShoppingList.
     *
     * @param items lista de entidades de domínio
     * @return lista de DTOs de resposta
     */
    List<ItemResponse> toItemResponseList(List<ListItem> items);
}


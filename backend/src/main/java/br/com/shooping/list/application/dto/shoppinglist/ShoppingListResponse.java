
package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

/**
 * DTO de resposta completo para uma lista de compras.
 * Inclui detalhes completos da lista e contadores de itens.
 * O campo items é opcional e só é preenchido quando necessário (ex: GET /api/v1/lists/{id}).
 */
public record ShoppingListResponse(
        /**
         * ID único da lista
         */
        Long id,

        /**
         * ID do proprietário da lista
         */
        Long ownerId,

        /**
         * Título da lista
         */
        String title,

        /**
         * Descrição da lista (opcional)
         */
        String description,

        /**
         * Lista de itens da lista de compras (opcional).
         * Preenchido apenas quando necessário (ex: GET /api/v1/lists/{id}).
         * Null ou vazio em outros endpoints para otimizar payload.
         */
        List<ItemResponse> items,

        /**
         * Total de itens na lista
         */
        int itemsCount,

        /**
         * Total de itens pendentes (não comprados)
         */
        int pendingItemsCount,

        /**
         * Total de itens comprados
         */
        int purchasedItemsCount,

        /**
         * Data de criação da lista
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,

        /**
         * Data da última atualização
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt
) {}


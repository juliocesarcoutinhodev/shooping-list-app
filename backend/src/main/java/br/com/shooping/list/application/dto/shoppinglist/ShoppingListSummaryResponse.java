package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * DTO de resposta resumido para listagem de listas de compras.
 * Versão mais leve que inclui apenas informações essenciais.
 * Usado no endpoint de listagem (GET /api/v1/shopping-lists).
 */
public record ShoppingListSummaryResponse(
        /**
         * ID único da lista
         */
        Long id,

        /**
         * Título da lista
         */
        String title,

        /**
         * Total de itens na lista
         */
        int itemsCount,

        /**
         * Total de itens pendentes (não comprados)
         */
        int pendingItemsCount,

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


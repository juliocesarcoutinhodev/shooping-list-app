package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de resposta para um item de lista de compras.
 * Retornado pelos endpoints de gerenciamento de itens.
 */
public record ItemResponse(
        /**
         * ID único do item
         */
        Long id,

        /**
         * Nome do item
         */
        String name,

        /**
         * Quantidade
         */
        BigDecimal quantity,

        /**
         * Unidade de medida (opcional)
         */
        String unit,

        /**
         * Preço unitário (opcional)
         */
        BigDecimal unitPrice,

        /**
         * Status do item (PENDING ou PURCHASED)
         */
        String status,

        /**
         * Data de criação do item
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,

        /**
         * Data da última atualização
         */
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt
) {}


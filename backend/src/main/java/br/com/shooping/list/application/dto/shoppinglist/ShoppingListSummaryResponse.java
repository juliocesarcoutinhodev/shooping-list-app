package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de resposta resumido para listagem de listas de compras.
 * Versão mais leve que inclui apenas informações essenciais.
 * Usado no endpoint de listagem (GET /api/v1/shopping-lists).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListSummaryResponse {

    /**
     * ID único da lista
     */
    private Long id;

    /**
     * Título da lista
     */
    private String title;

    /**
     * Total de itens na lista
     */
    private int itemsCount;

    /**
     * Total de itens pendentes (não comprados)
     */
    private int pendingItemsCount;

    /**
     * Data de criação da lista
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Data da última atualização
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}


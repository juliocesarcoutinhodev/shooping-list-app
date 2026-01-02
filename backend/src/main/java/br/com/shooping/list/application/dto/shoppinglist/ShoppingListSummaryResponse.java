package br.com.shooping.list.application.dto.shoppinglist;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO de resposta resumido para listagem de listas de compras.
 * Versão mais leve que inclui apenas informações essenciais.
 * Usado no endpoint de listagem (GET /api/v1/shopping-lists).
 */
@Schema(
    name = "ShoppingListSummaryResponse",
    description = "Lightweight shopping list summary (no items, optimized for listing)"
)
public record ShoppingListSummaryResponse(
        @Schema(
            description = "Shopping list unique identifier",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
            description = "Shopping list title",
            example = "Monthly Groceries",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String title,

        @Schema(
            description = "Total number of items in the list",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        int itemsCount,

        @Schema(
            description = "Number of pending items (not purchased yet)",
            example = "3",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        int pendingItemsCount,

        @Schema(
            description = "List creation timestamp (ISO-8601 UTC)",
            example = "2026-01-02T10:00:00.000Z",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant createdAt,

        @Schema(
            description = "Last update timestamp (ISO-8601 UTC)",
            example = "2026-01-02T15:30:00.000Z",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant updatedAt
) {}


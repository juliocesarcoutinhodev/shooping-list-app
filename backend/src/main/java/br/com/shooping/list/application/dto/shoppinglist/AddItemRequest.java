package br.com.shooping.list.application.dto.shoppinglist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para requisição de adição de item em lista de compras.
 * Usado no endpoint POST /api/v1/lists/{id}/items
 */
@Schema(
    name = "ShoppingListItemAddRequest",
    description = "Request to add a new item to a shopping list"
)
public record AddItemRequest(
        @Schema(
            description = "Item name (must be unique within the list)",
            example = "Rice",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 100
        )
        @NotBlank(message = "Nome do item é obrigatório")
        @Size(min = 3, max = 100, message = "Nome do item deve ter entre 3 e 100 caracteres")
        String name,

        @Schema(
            description = "Item quantity (must be greater than zero)",
            example = "2.5",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        BigDecimal quantity,

        @Schema(
            description = "Measurement unit (kg, un, L, box, etc.)",
            example = "kg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 20
        )
        @Size(max = 20, message = "Unidade não pode ter mais de 20 caracteres")
        String unit,

        @Schema(
            description = "Unit price (for estimated total calculation)",
            example = "5.50",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Preço unitário não pode ser negativo")
        BigDecimal unitPrice
) {}


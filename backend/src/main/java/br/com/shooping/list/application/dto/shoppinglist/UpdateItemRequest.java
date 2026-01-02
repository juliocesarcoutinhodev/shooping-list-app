package br.com.shooping.list.application.dto.shoppinglist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para requisição de atualização de item em lista de compras.
 * Usado no endpoint PATCH /api/v1/lists/{id}/items/{itemId}
 *
 * Atualização parcial: cliente envia apenas os campos que deseja alterar.
 * Pelo menos um campo deve ser fornecido.
 */
@Schema(
    name = "ShoppingListItemUpdateRequest",
    description = "Partial update request for shopping list item (send only fields to update)"
)
public record UpdateItemRequest(
        @Schema(
            description = "New item name (optional, must remain unique within list)",
            example = "Brown Rice",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minLength = 3,
            maxLength = 100
        )
        @Size(min = 3, max = 100, message = "Nome do item deve ter entre 3 e 100 caracteres")
        String name,

        @Schema(
            description = "New quantity (optional, must be greater than zero)",
            example = "3.0",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        BigDecimal quantity,

        @Schema(
            description = "New measurement unit (optional)",
            example = "kg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 20
        )
        @Size(max = 20, message = "Unidade não pode ter mais de 20 caracteres")
        String unit,

        @Schema(
            description = "New unit price (optional)",
            example = "6.00",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @DecimalMin(value = "0.0", message = "Preço unitário não pode ser negativo")
        BigDecimal unitPrice,

        @Schema(
            description = "New item status (optional): PENDING or PURCHASED",
            example = "PURCHASED",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            allowableValues = {"PENDING", "PURCHASED"}
        )
        String status
) {
    /**
     * Valida se pelo menos um campo foi fornecido para atualização.
     *
     * @return true se pelo menos um campo está presente
     */
    public boolean hasAtLeastOneField() {
        return (name != null && !name.isBlank())
                || quantity != null
                || unit != null
                || unitPrice != null
                || status != null;
    }

    /**
     * Verifica se o status fornecido é válido.
     *
     * @return true se status é PENDING ou PURCHASED
     */
    public boolean isValidStatus() {
        if (status == null) {
            return true;
        }
        return "PENDING".equals(status) || "PURCHASED".equals(status);
    }
}


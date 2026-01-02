package br.com.shooping.list.application.dto.shoppinglist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de atualização de lista de compras.
 * Permite atualizar título e/ou descrição da lista.
 * O ID da lista vem da URL no endpoint PATCH /api/v1/lists/{id}
 *
 * Atualização parcial: cliente envia apenas os campos que deseja alterar.
 * Pelo menos um campo deve ser fornecido.
 */
@Schema(
    name = "ShoppingListUpdateRequest",
    description = "Partial update request for shopping list (send only fields to update)"
)
public record UpdateShoppingListRequest(
        @Schema(
            description = "New shopping list title (optional, send only if updating)",
            example = "Monthly Groceries - Updated",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            minLength = 3,
            maxLength = 100
        )
        @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
        String title,

        @Schema(
            description = "New shopping list description (optional, send empty string to remove)",
            example = "Updated description",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 255,
            nullable = true
        )
        @Size(max = 255, message = "Descrição não pode ter mais de 255 caracteres")
        String description
) {
    /**
     * Valida se pelo menos um campo foi fornecido para atualização.
     *
     * @return true se pelo menos um campo está presente
     */
    public boolean hasAtLeastOneField() {
        return (title != null && !title.isBlank()) || description != null;
    }
}


package br.com.shooping.list.application.dto.shoppinglist;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de criação de nova lista de compras.
 */
@Schema(
    name = "ShoppingListCreateRequest",
    description = "Request to create a new shopping list"
)
public record CreateShoppingListRequest(
        @Schema(
            description = "Shopping list title",
            example = "Monthly Groceries",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 100
        )
        @NotBlank(message = "Título da lista é obrigatório")
        @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
        String title,

        @Schema(
            description = "Shopping list description (optional)",
            example = "Supermarket shopping for January",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            maxLength = 255
        )
        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description
) {}

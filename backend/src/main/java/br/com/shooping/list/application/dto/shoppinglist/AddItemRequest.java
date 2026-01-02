package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO para requisição de adição de item em lista de compras.
 * Usado no endpoint POST /api/v1/lists/{id}/items
 */
public record AddItemRequest(
        @NotBlank(message = "Nome do item é obrigatório")
        @Size(min = 3, max = 100, message = "Nome do item deve ter entre 3 e 100 caracteres")
        String name,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        BigDecimal quantity,

        @Size(max = 20, message = "Unidade não pode ter mais de 20 caracteres")
        String unit,

        @DecimalMin(value = "0.0", message = "Preço unitário não pode ser negativo")
        BigDecimal unitPrice
) {}


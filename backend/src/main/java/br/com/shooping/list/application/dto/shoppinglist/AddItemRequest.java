package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para requisição de adição de item em lista de compras.
 * Usado no endpoint POST /api/v1/lists/{id}/items
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {

    @NotBlank(message = "Nome do item é obrigatório")
    @Size(min = 3, max = 100, message = "Nome do item deve ter entre 3 e 100 caracteres")
    private String name;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;

    @Size(max = 20, message = "Unidade não pode ter mais de 20 caracteres")
    private String unit;

    @DecimalMin(value = "0.0", message = "Preço unitário não pode ser negativo")
    private BigDecimal unitPrice;
}


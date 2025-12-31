package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para requisição de atualização de item em lista de compras.
 * Usado no endpoint PATCH /api/v1/lists/{id}/items/{itemId}
 *
 * Atualização parcial: cliente envia apenas os campos que deseja alterar.
 * Pelo menos um campo deve ser fornecido.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @Size(min = 3, max = 100, message = "Nome do item deve ter entre 3 e 100 caracteres")
    private String name;

    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantity;

    @Size(max = 20, message = "Unidade não pode ter mais de 20 caracteres")
    private String unit;

    @DecimalMin(value = "0.0", message = "Preço unitário não pode ser negativo")
    private BigDecimal unitPrice;

    private String status; // PENDING ou PURCHASED

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


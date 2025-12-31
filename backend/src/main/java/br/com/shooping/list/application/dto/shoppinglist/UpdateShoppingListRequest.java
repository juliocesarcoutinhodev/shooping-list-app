package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de atualização de lista de compras.
 * Permite atualizar título e/ou descrição da lista.
 * O ID da lista vem da URL no endpoint PATCH /api/v1/lists/{id}
 *
 * Atualização parcial: cliente envia apenas os campos que deseja alterar.
 * Pelo menos um campo deve ser fornecido.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingListRequest {

    @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    private String title;

    @Size(max = 255, message = "Descrição não pode ter mais de 255 caracteres")
    private String description;

    /**
     * Valida se pelo menos um campo foi fornecido para atualização.
     *
     * @return true se pelo menos um campo está presente
     */
    public boolean hasAtLeastOneField() {
        return (title != null && !title.isBlank()) || description != null;
    }
}


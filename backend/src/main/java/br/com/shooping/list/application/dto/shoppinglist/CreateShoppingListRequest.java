package br.com.shooping.list.application.dto.shoppinglist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de criação de nova lista de compras.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShoppingListRequest {

    @NotBlank(message = "Título da lista é obrigatório")
    @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres")
    private String title;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String description;
}

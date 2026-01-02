package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interface de documentação OpenAPI para endpoints de itens de lista.
 */
@Tag(
    name = "Shopping List Items",
    description = """
        Endpoints para gerenciamento de itens dentro de listas de compras.
        
        **Funcionalidades:**
        - Adicionar itens a uma lista
        - Atualizar itens (nome, quantidade, unidade, preço, status)
        - Marcar item como comprado/pendente (toggle status)
        - Remover itens
        
        **Regras de negócio:**
        - Máximo 100 itens por lista
        - Nomes de itens devem ser únicos dentro da lista
        - Status: PENDING (padrão) ou PURCHASED
        
        **Todos os endpoints requerem autenticação JWT.**
        """
)
public interface ShoppingListItemAPI {

    @Operation(
        summary = "Adicionar item à lista",
        description = """
            Adiciona um novo item a uma lista de compras existente.
            
            **Campos obrigatórios:**
            - name (2-100 caracteres, único na lista)
            - quantity (maior que 0)
            - unit (kg, un, L, etc.)
            
            **Campos opcionais:**
            - unitPrice (preço unitário para cálculo de total estimado)
            
            **Validações:**
            - Lista deve existir
            - Apenas o dono pode adicionar itens
            - Nome do item deve ser único na lista
            - Limite de 100 itens por lista
            
            **Comportamento:**
            - Status inicial: PENDING
            - Nome normalizado para validação de duplicatas
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping List Items"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Item adicionado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ItemResponse.class),
                examples = @ExampleObject(
                    name = "Item criado",
                    value = """
                        {
                          "id": 1,
                          "name": "Arroz",
                          "quantity": 2.0,
                          "unit": "kg",
                          "unitPrice": 5.50,
                          "status": "PENDING",
                          "createdAt": "2026-01-02T10:05:00Z",
                          "updatedAt": "2026-01-02T10:05:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos (validação falhou)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (não é o dono da lista)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lista não encontrada",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Item com este nome já existe na lista",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Lista atingiu limite de 100 itens",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<ItemResponse> addItemToList(
        @Parameter(
            name = "listId",
            description = "ID da lista onde adicionar o item",
            required = true,
            example = "1"
        )
        @PathVariable Long listId,
        @Valid @RequestBody AddItemRequest request
    );

    @Operation(
        summary = "Atualizar item",
        description = """
            Atualiza informações de um item existente.
            
            **Atualização parcial:**
            - Envie apenas os campos que deseja alterar
            - Campos não enviados permanecem inalterados
            
            **Campos atualizáveis:**
            - name (deve permanecer único na lista)
            - quantity (deve ser maior que 0)
            - unit (kg, un, L, etc.)
            - unitPrice (preço unitário)
            - status (PENDING ou PURCHASED)
            
            **Uso comum:**
            - Toggle status: envie apenas {"status": "PURCHASED"} ou {"status": "PENDING"}
            - Ajustar quantidade: envie apenas {"quantity": 3.0}
            
            **Validações:**
            - Lista deve existir
            - Item deve existir na lista
            - Apenas o dono pode atualizar
            - Se alterar nome, deve permanecer único
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping List Items"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Item atualizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ItemResponse.class),
                examples = @ExampleObject(
                    name = "Item atualizado",
                    value = """
                        {
                          "id": 1,
                          "name": "Arroz Integral",
                          "quantity": 3.0,
                          "unit": "kg",
                          "unitPrice": 6.00,
                          "status": "PURCHASED",
                          "createdAt": "2026-01-02T10:05:00Z",
                          "updatedAt": "2026-01-02T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou nenhum campo fornecido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (não é o dono da lista)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lista ou item não encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Novo nome já existe em outro item da lista",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<ItemResponse> updateItem(
        @Parameter(
            name = "listId",
            description = "ID da lista que contém o item",
            required = true,
            example = "1"
        )
        @PathVariable Long listId,
        @Parameter(
            name = "itemId",
            description = "ID do item a ser atualizado",
            required = true,
            example = "1"
        )
        @PathVariable Long itemId,
        @Valid @RequestBody UpdateItemRequest request
    );

    @Operation(
        summary = "Remover item da lista",
        description = """
            Remove permanentemente um item de uma lista de compras.
            
            **Atenção:**
            - Operação irreversível
            
            **Validações:**
            - Lista deve existir
            - Item deve existir na lista
            - Apenas o dono pode remover
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping List Items"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Item removido com sucesso (sem conteúdo)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (não é o dono da lista)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lista ou item não encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<Void> removeItem(
        @Parameter(
            name = "listId",
            description = "ID da lista que contém o item",
            required = true,
            example = "1"
        )
        @PathVariable Long listId,
        @Parameter(
            name = "itemId",
            description = "ID do item a ser removido",
            required = true,
            example = "1"
        )
        @PathVariable Long itemId
    );
}


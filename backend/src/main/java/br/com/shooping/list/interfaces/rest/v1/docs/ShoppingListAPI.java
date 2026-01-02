package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
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

import java.util.List;

/**
 * Interface de documentação OpenAPI para endpoints de listas de compras.
 */
@Tag(
    name = "Shopping Lists",
    description = """
        Endpoints CRUD para gerenciamento de listas de compras.
        
        **Funcionalidades:**
        - Criar novas listas
        - Listar todas as listas do usuário (resumo)
        - Obter detalhes completos de uma lista (com itens)
        - Atualizar título e descrição
        - Deletar lista (cascata - remove itens também)
        
        **Regras de negócio:**
        - Cada lista pertence a um único usuário (owner)
        - Apenas o dono pode modificar/deletar a lista
        - Limite de 100 itens por lista
        
        **Todos os endpoints requerem autenticação JWT.**
        """
)
public interface ShoppingListAPI {

    @Operation(
        summary = "Criar nova lista de compras",
        description = """
            Cria uma nova lista de compras para o usuário autenticado.
            
            **Campos obrigatórios:**
            - title (2-100 caracteres)
            
            **Campos opcionais:**
            - description (até 500 caracteres)
            
            **Comportamento:**
            - Lista criada vazia (sem itens)
            - Owner automático (extraído do JWT)
            - ID gerado automaticamente
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping Lists"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Lista criada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListResponse.class),
                examples = @ExampleObject(
                    name = "Lista criada",
                    value = """
                        {
                          "id": 1,
                          "ownerId": 1,
                          "title": "Compras do mês",
                          "description": "Lista para supermercado",
                          "items": null,
                          "itemsCount": 0,
                          "pendingItemsCount": 0,
                          "purchasedItemsCount": 0,
                          "createdAt": "2026-01-02T10:00:00Z",
                          "updatedAt": "2026-01-02T10:00:00Z"
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
        )
    })
    ResponseEntity<ShoppingListResponse> createList(@Valid @RequestBody CreateShoppingListRequest request);

    @Operation(
        summary = "Listar minhas listas de compras",
        description = """
            Retorna resumo de todas as listas do usuário autenticado.
            
            **Formato de retorno:**
            - Lista resumida (sem itens individuais)
            - Ordenação por data de criação (mais recente primeiro)
            - Contadores: total de itens, pendentes, comprados
            
            **Performance:**
            - Não carrega itens (mais rápido)
            - Para detalhes completos, usar GET /lists/{id}
            
            **Retorna lista vazia se usuário não tiver listas.**
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping Lists"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Listas retornadas com sucesso (pode ser vazia)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListSummaryResponse.class),
                examples = @ExampleObject(
                    name = "Lista de resumos",
                    value = """
                        [
                          {
                            "id": 1,
                            "title": "Compras do mês",
                            "itemsCount": 5,
                            "pendingItemsCount": 3,
                            "createdAt": "2026-01-02T10:00:00Z",
                            "updatedAt": "2026-01-02T15:30:00Z"
                          },
                          {
                            "id": 2,
                            "title": "Farmácia",
                            "itemsCount": 2,
                            "pendingItemsCount": 2,
                            "createdAt": "2026-01-01T08:00:00Z",
                            "updatedAt": "2026-01-01T08:00:00Z"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<List<ShoppingListSummaryResponse>> getMyLists();

    @Operation(
        summary = "Obter detalhes de uma lista",
        description = """
            Retorna detalhes completos de uma lista específica, incluindo todos os itens.
            
            **Dados retornados:**
            - Informações da lista (título, descrição, contadores)
            - Todos os itens da lista com detalhes completos
            - Status de cada item (PENDING ou PURCHASED)
            
            **Validações:**
            - Lista deve existir
            - Apenas o dono pode visualizar
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping Lists"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista retornada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListResponse.class),
                examples = @ExampleObject(
                    name = "Lista completa",
                    value = """
                        {
                          "id": 1,
                          "ownerId": 1,
                          "title": "Compras do mês",
                          "description": "Lista para supermercado",
                          "items": [
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
                          ],
                          "itemsCount": 1,
                          "pendingItemsCount": 1,
                          "purchasedItemsCount": 0,
                          "createdAt": "2026-01-02T10:00:00Z",
                          "updatedAt": "2026-01-02T10:05:00Z"
                        }
                        """
                )
            )
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
        )
    })
    ResponseEntity<ShoppingListResponse> getListById(
        @Parameter(
            name = "id",
            description = "ID da lista de compras",
            required = true,
            example = "1"
        )
        @PathVariable Long id
    );

    @Operation(
        summary = "Atualizar lista de compras",
        description = """
            Atualiza título e/ou descrição de uma lista existente.
            
            **Atualização parcial:**
            - Envie apenas os campos que deseja alterar
            - Campos não enviados permanecem inalterados
            - Para remover descrição, envie string vazia
            
            **Validações:**
            - Lista deve existir
            - Apenas o dono pode atualizar
            - Pelo menos um campo deve ser fornecido
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping Lists"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista atualizada com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShoppingListResponse.class)
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
            description = "Acesso negado (não é o dono)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lista não encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<ShoppingListResponse> updateList(
        @Parameter(
            name = "id",
            description = "ID da lista a ser atualizada",
            required = true,
            example = "1"
        )
        @PathVariable Long id,
        @Valid @RequestBody UpdateShoppingListRequest request
    );

    @Operation(
        summary = "Deletar lista de compras",
        description = """
            Remove permanentemente uma lista e todos os seus itens.
            
            **Atenção:**
            - Operação irreversível
            - Todos os itens da lista também serão deletados (cascata)
            
            **Validações:**
            - Lista deve existir
            - Apenas o dono pode deletar
            
            **Requer autenticação JWT.**
            """,
        tags = {"Shopping Lists"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Lista deletada com sucesso (sem conteúdo)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acesso negado (não é o dono)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Lista não encontrada",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<Void> deleteList(
        @Parameter(
            name = "id",
            description = "ID da lista a ser deletada",
            required = true,
            example = "1"
        )
        @PathVariable Long id
    );
}


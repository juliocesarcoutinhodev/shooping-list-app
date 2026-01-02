package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.user.UserMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * Interface de documentação OpenAPI para endpoints de usuário.
 */
@Tag(
    name = "User",
    description = """
        Endpoints de gerenciamento do perfil do usuário autenticado.
        
        **Funcionalidades:**
        - Consultar dados do perfil
        - Atualizar informações pessoais (futuro)
        - Deletar conta (futuro)
        
        **Todos os endpoints requerem autenticação JWT.**
        """
)
public interface UserAPI {

    @Operation(
        summary = "Obter dados do usuário atual",
        description = """
            Retorna informações completas do usuário autenticado.
            
            **Dados retornados:**
            - ID, email, nome
            - Provider (LOCAL ou GOOGLE)
            - Status da conta (ACTIVE, INACTIVE, BLOCKED)
            - Timestamps de criação e atualização
            
            **Segurança:**
            - Dados extraídos do JWT (não requer consulta ao banco)
            - Não expõe dados sensíveis (senha, tokens)
            
            **Requer autenticação JWT.**
            """,
        tags = {"User"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Dados do usuário retornados com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserMeResponse.class),
                examples = @ExampleObject(
                    name = "Usuário LOCAL",
                    value = """
                        {
                          "id": 1,
                          "email": "usuario@exemplo.com",
                          "name": "João Silva",
                          "provider": "LOCAL",
                          "status": "ACTIVE",
                          "createdAt": "2026-01-01T10:00:00Z",
                          "updatedAt": "2026-01-02T15:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado (token ausente ou inválido)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado (token válido mas usuário deletado)",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<UserMeResponse> getCurrentUser();
}


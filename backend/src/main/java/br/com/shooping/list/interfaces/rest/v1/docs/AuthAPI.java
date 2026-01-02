package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Interface de documentação OpenAPI para endpoints de autenticação.
 * <p>
 * Separa a documentação da implementação, mantendo os controllers limpos.
 * Padrão usado por grandes empresas (Stripe, GitHub, AWS).
 */
@Tag(
    name = "Authentication",
    description = """
        Endpoints de autenticação e autorização.
        
        **Funcionalidades:**
        - Registro de novos usuários (LOCAL)
        - Login com email/senha
        - Login via Google OAuth2
        - Renovação de tokens (refresh)
        - Logout e revogação de tokens
        
        **Segurança:**
        - Tokens JWT com expiração configurável
        - Refresh tokens armazenados em cookies HttpOnly
        - Suporte a rotação automática de tokens
        """
)
public interface AuthAPI {

    @Operation(
        summary = "Registrar novo usuário",
        description = """
            Cria uma nova conta de usuário LOCAL com email e senha.
            
            **Regras de negócio:**
            - Email deve ser único no sistema
            - Senha deve ter no mínimo 8 caracteres
            - Senha é armazenada com hash BCrypt
            - Role USER atribuída automaticamente
            
            **Endpoint público** - não requer autenticação.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Usuário registrado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisterResponse.class),
                examples = @ExampleObject(
                    name = "Usuário criado",
                    value = """
                        {
                          "id": 1,
                          "email": "usuario@exemplo.com",
                          "name": "João Silva",
                          "provider": "LOCAL",
                          "status": "ACTIVE",
                          "createdAt": "2026-01-02T10:30:00Z"
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
            responseCode = "409",
            description = "Email já cadastrado",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "")
    ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(
        summary = "Login com email e senha",
        description = """
            Autentica usuário LOCAL usando email e senha.
            
            **Retorna:**
            - Access token (JWT) válido por 15 minutos
            - Refresh token válido por 7 dias (também enviado via cookie HttpOnly)
            
            **Segurança:**
            - Senha validada com BCrypt
            - Tokens gerados com informações do dispositivo (user-agent, IP)
            
            **Endpoint público** - não requer autenticação.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(
                    name = "Tokens JWT",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                          "expiresIn": 900
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "")
    ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    );

    @Operation(
        summary = "Login com Google OAuth2",
        description = """
            Autentica usuário via Google OAuth2 usando ID Token.
            
            **Fluxo:**
            1. Frontend obtém ID Token do Google
            2. Envia ID Token para este endpoint
            3. Backend valida token com Google API
            4. Cria ou atualiza usuário
            5. Retorna tokens JWT próprios
            
            **Primeira autenticação:**
            - Cria usuário automaticamente
            - Provider definido como GOOGLE
            - Role USER atribuída
            
            **Endpoint público** - não requer autenticação.
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login Google realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID Token inválido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token Google inválido ou expirado",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "")
    ResponseEntity<LoginResponse> googleLogin(
        @Valid @RequestBody GoogleLoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    );

    @Operation(
        summary = "Renovar access token",
        description = """
            Renova o access token usando refresh token válido.
            
            **Refresh token aceito via:**
            - Cookie HttpOnly (preferencial, mais seguro)
            - Body da requisição (fallback para dev/test)
            
            **Rotação de tokens:**
            - Novo access token gerado
            - Novo refresh token gerado (rotação)
            - Refresh token anterior invalidado
            
            **Endpoint público** - não requer autenticação JWT (usa refresh token).
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token renovado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RefreshTokenResponse.class),
                examples = @ExampleObject(
                    name = "Novos tokens",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                          "expiresIn": 900
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Refresh token não fornecido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh token inválido, expirado ou revogado",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "")
    ResponseEntity<RefreshTokenResponse> refresh(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    );

    @Operation(
        summary = "Logout (revogar tokens)",
        description = """
            Revoga o refresh token atual, invalidando a sessão.
            
            **Efeitos:**
            - Refresh token deletado do banco de dados
            - Cookie de refresh token removido
            - Access token continua válido até expirar (não pode ser revogado)
            
            **Recomendação:**
            - Frontend deve descartar access token localmente
            
            **Requer autenticação JWT** (access token válido).
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Logout realizado com sucesso (sem conteúdo)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Refresh token não fornecido",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Não autenticado (access token inválido)",
            content = @Content(mediaType = "application/json")
        )
    })
    ResponseEntity<Void> logout(
        @RequestBody(required = false) LogoutRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    );
}


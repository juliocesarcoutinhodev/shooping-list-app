package br.com.shooping.list.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração da documentação OpenAPI 3.0 com Swagger UI.
 * <p>
 * Expõe a documentação da API em:
 * - Swagger UI: /swagger-ui/index.html
 * - OpenAPI JSON: /v3/api-docs
 * - OpenAPI YAML: /v3/api-docs.yaml
 * <p>
 * A documentação é versionada (v1) e preparada para evolução futura.
 * <p>
 * **Segurança:**
 * - Configurado com autenticação Bearer JWT
 * - SecurityScheme definido para permitir testes autenticados no Swagger UI
 * - Endpoints públicos e protegidos corretamente identificados
 * <p>
 * **Ambientes:**
 * - dev/test: Swagger habilitado
 * - prod: Swagger desabilitado (configurado em application-prod.yml)
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${spring.application.name:Shopping List API}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configura os metadados da API OpenAPI.
     * Define informações completas sobre a API, incluindo:
     * - Título e descrição funcional
     * - Versão atual (v1)
     * - Informações de contato
     * - Licença
     * - Servidores disponíveis
     * - Esquema de segurança JWT (Bearer Token)
     * - Requisito de segurança global
     *
     * @return configuração OpenAPI completa
     */
    @Bean
    public OpenAPI shoppingListOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * Define o esquema de segurança JWT Bearer Token.
     * Permite que o Swagger UI solicite e utilize tokens JWT para autenticação.
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme());
    }

    /**
     * Configura o SecurityScheme para autenticação Bearer JWT.
     * <p>
     * O usuário deve fornecer o token no formato:
     * Authorization: Bearer {token}
     * <p>
     * Para obter o token:
     * 1. Usar o endpoint POST /api/v1/auth/login ou /api/v1/auth/register
     * 2. Copiar o campo "accessToken" da resposta
     * 3. Clicar no botão "Authorize" no Swagger UI
     * 4. Inserir o token (sem o prefixo "Bearer")
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("""
                        Autenticação JWT Bearer Token
                        
                        **Como obter o token:**
                        1. Use POST /api/v1/auth/register para criar uma conta
                        2. Ou use POST /api/v1/auth/login com credenciais existentes
                        3. Copie o valor do campo `accessToken` da resposta
                        4. Clique no botão 🔓 **Authorize** no topo da página
                        5. Cole o token no campo (sem adicionar "Bearer")
                        6. Clique em "Authorize"
                        
                        **Endpoints públicos (não requerem token):**
                        - POST /api/v1/auth/register
                        - POST /api/v1/auth/login
                        - POST /api/v1/auth/google
                        - POST /api/v1/auth/refresh
                        - GET /actuator/health
                        
                        **Endpoints protegidos (requerem token):**
                        - Todos os endpoints de /api/v1/lists/**
                        - GET /api/v1/users/me
                        - POST /api/v1/auth/logout
                        """);
    }

    /**
     * Define o requisito de segurança global.
     * Todos os endpoints serão documentados como protegidos por padrão.
     * Endpoints públicos devem ser marcados explicitamente com @SecurityRequirement(name = "").
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

    /**
     * Define os metadados principais da API.
     */
    private Info apiInfo() {
        return new Info()
                .title("Shopping List API")
                .description("""
                        API RESTful para gerenciamento de listas de compras.
                        
                        **Funcionalidades principais:**
                        - Autenticação JWT com suporte a refresh token
                        - Login via Google OAuth2
                        - CRUD completo de listas de compras
                        - Gerenciamento de itens (adicionar, editar, remover, marcar como comprado)
                        - Gestão de perfil de usuário
                        - Autorização baseada em ownership (apenas o dono pode modificar suas listas)
                        
                        **Arquitetura:**
                        - Clean Architecture com Domain-Driven Design (DDD)
                        - Camadas: Domain, Application, Infrastructure, Interfaces
                        - DTOs com Java Records e Bean Validation
                        - Mapeamento automático com MapStruct
                        
                        **Segurança:**
                        - Tokens JWT com rotação automática
                        - Cookies HttpOnly para refresh tokens
                        - Validação de ownership em todas as operações
                        - Proteção contra CSRF
                        """)
                .version("v1")
                .contact(apiContact())
                .license(apiLicense());
    }

    /**
     * Define informações de contato do projeto.
     */
    private Contact apiContact() {
        return new Contact()
                .name("Shopping List Suporte")
                .email("juliocesar.coutinhodev@outlook.com")
                .url("https://github.com/juliocesarcoutinhodev/shooping-list-app");
    }

    /**
     * Define a licença do projeto.
     */
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Define os servidores disponíveis para a API.
     * Preparado para múltiplos ambientes (dev, staging, production).
     */
    private List<Server> apiServers() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Development Server");

        // Preparado para adicionar mais servidores:
        // Server stagingServer = new Server()
        //     .url("https://staging-api.shoppinglist.com")
        //     .description("Staging Server");
        //
        // Server prodServer = new Server()
        //     .url("https://api.shoppinglist.com")
        //     .description("Production Server");

        return List.of(devServer);
    }
}


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
 * OpenAPI 3.0 configuration (Swagger UI).
 *
 * <p>Documentation endpoints:
 * <ul>
 *   <li>Swagger UI: /swagger-ui/index.html</li>
 *   <li>OpenAPI JSON: /v3/api-docs</li>
 *   <li>OpenAPI YAML: /v3/api-docs.yaml</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *   <li>HTTP Bearer JWT configured to enable authenticated calls from Swagger UI</li>
 *   <li>A global security requirement is applied by default</li>
 *   <li>Public endpoints must explicitly override security in controller annotations</li>
 * </ul>
 *
 * <p>Environments:
 * <ul>
 *   <li>dev/test: Swagger enabled</li>
 *   <li>prod: Swagger disabled via configuration (recommended)</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${spring.application.name:Shopping List API}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI shoppingListOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .components(securityComponents())
                .addSecurityItem(globalSecurityRequirement());
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerJwtSecurityScheme());
    }

    /**
     * Global requirement: all operations are protected by default.
     *
     * <p>For public endpoints, override security at controller/method level using:
     * <ul>
     *   <li>@Operation(security = @SecurityRequirement(name = ""))</li>
     *   <li>or @SecurityRequirements (empty) depending on your annotation strategy</li>
     * </ul>
     */
    private SecurityRequirement globalSecurityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

    private SecurityScheme bearerJwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("""
                        JWT Bearer authentication

                        How to authenticate using Swagger UI:
                        1. Call POST /api/v1/auth/register to create an account (optional)
                        2. Call POST /api/v1/auth/login with valid credentials
                        3. Copy the `accessToken` value from the response
                        4. Click "Authorize" (top-right)
                        5. Paste the token value (do not include the "Bearer" prefix)
                        6. Click "Authorize" to apply it to protected endpoints

                        Public endpoints (no JWT required):
                        - POST /api/v1/auth/register
                        - POST /api/v1/auth/login
                        - POST /api/v1/auth/google
                        - POST /api/v1/auth/refresh
                        - GET  /actuator/health

                        Protected endpoints (JWT required):
                        - /api/v1/lists/**
                        - GET /api/v1/users/me
                        - POST /api/v1/auth/logout
                        """);
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName)
                .description("""
                        REST API for managing shopping lists.

                        Key capabilities:
                        - JWT authentication with refresh token support
                        - Google OAuth2 login
                        - Full CRUD for shopping lists
                        - Item management (add, update, remove, mark as purchased)
                        - User profile endpoint
                        - Ownership-based authorization (only the owner can modify lists)

                        Architecture:
                        - Clean Architecture with Domain-Driven Design (DDD)
                        - Layers: Domain, Application, Infrastructure, Interfaces
                        - DTOs using Java Records + Bean Validation
                        - Mapping via MapStruct

                        Security:
                        - JWT access tokens
                        - HttpOnly cookies for refresh tokens
                        - Ownership validation on protected operations
                        - CSRF protection when applicable
                        """)
                .version("v1")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Shopping List Support")
                .email("juliocesar.coutinhodev@outlook.com")
                .url("https://github.com/juliocesarcoutinhodev/shooping-list-app");
    }

    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Declares available servers for the API.
     * Prepared for multiple environments (dev/staging/prod).
     */
    private List<Server> apiServers() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Development Server");

        // Future servers example:
        // Server stagingServer = new Server()
        //         .url("https://staging-api.shoppinglist.com")
        //         .description("Staging Server");
        //
        // Server prodServer = new Server()
        //         .url("https://api.shoppinglist.com")
        //         .description("Production Server");

        return List.of(devServer);
    }
}

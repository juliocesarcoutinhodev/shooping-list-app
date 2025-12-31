package br.com.shooping.list.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Configuração de segurança da aplicação.
 * <p>
 * Define:
 * - API stateless (sem sessão)
 * - CSRF desabilitado (API REST)
 * - CORS configurado para desenvolvimento
 * - Rotas públicas e protegidas
 * - Filtro JWT para autenticação via Bearer token
 * - Method Security habilitado para @PreAuthorize, @PostAuthorize, @Secured
 * - RBAC (Role-Based Access Control): rotas /admin/** exigem role ADMIN
 * - Handlers customizados para 401 (não autenticado) e 403 (sem permissão)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAccessDeniedHandler accessDeniedHandler
    ) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    private static final String[] PUBLIC_ENDPOINTS = {
            SecurityRoutes.Public.HEALTH,
            SecurityRoutes.Public.AUTH_BASE,
            SecurityRoutes.Public.ACTUATOR_HEALTH,
            SecurityRoutes.Public.H2_CONSOLE,
            // Swagger/OpenAPI (quando habilitado)
            SecurityRoutes.Public.SWAGGER_API_DOCS,
            SecurityRoutes.Public.SWAGGER_UI,
            SecurityRoutes.Public.SWAGGER_UI_HTML,
            SecurityRoutes.Public.ERROR
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (API stateless não precisa)
                .csrf(AbstractHttpConfigurer::disable)

                // Configura CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configura gerenciamento de sessão como STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configura autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // Rotas públicas
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Rotas administrativas - exigem role ADMIN
                        .requestMatchers(SecurityRoutes.Public.Admin.ADMIN_BASE).hasRole("ADMIN")
                        // Todas as outras requisições precisam apenas autenticação
                        .anyRequest().authenticated()
                )

                // Adiciona filtro JWT antes do filtro de autenticação padrão
                // Isso garante que o JWT seja processado ANTES da autorização
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura headers de segurança
                .headers(headers -> headers
                        // Permite frames do mesmo domínio (necessário para H2 Console em dev)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        // Previne MIME type sniffing
                        .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                        // Habilita proteção XSS do navegador
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        // Cache control (não cachear respostas sensíveis)
                        .cacheControl(HeadersConfigurer.CacheControlConfig::disable)
                        // Referrer Policy (protege informações de navegação)
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )

                // Configura tratamento de exceções de autenticação e autorização
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)  // 401 - Não autenticado
                        .accessDeniedHandler(accessDeniedHandler)            // 403 - Sem permissão
                );

        return http.build();
    }

    /**
     * Configuração CORS para desenvolvimento.
     * Em produção, deve ser mais restritivo.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origens permitidas (dev)
        configuration.setAllowedOrigins(CorsProperties.AllowedOrigins.getAll());

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(CorsProperties.AllowedMethods.getAll());

        // Headers permitidos
        configuration.setAllowedHeaders(CorsProperties.AllowedHeaders.getAll());

        // Headers expostos
        configuration.setExposedHeaders(CorsProperties.ExposedHeaders.getAll());

        // Permite credenciais
        configuration.setAllowCredentials(CorsProperties.Config.ALLOW_CREDENTIALS);

        // Tempo de cache do preflight
        configuration.setMaxAge(CorsProperties.Config.MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Encoder de senhas usando BCrypt.
     * Usado para hash de senhas de usuários LOCAL.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


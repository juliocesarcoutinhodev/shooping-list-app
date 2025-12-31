package br.com.shooping.list.infrastructure.security;

import java.util.List;

/**
 * Propriedades de configuração CORS centralizadas.
 * <p>
 * Esta classe serve como referência centralizada para todas as configurações
 * relacionadas a CORS (Cross-Origin Resource Sharing).
 * <p>
 * <strong>Importante:</strong> Estas configurações são para DESENVOLVIMENTO.
 * Em produção, deve-se restringir origens apenas aos domínios autorizados.
 */
public final class CorsProperties {

    private CorsProperties() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Origens permitidas para requisições CORS (Desenvolvimento).
     */
    public static final class AllowedOrigins {

        /** Frontend React (porta padrão) */
        public static final String REACT = "http://localhost:3000";

        /**Front VueJs (porta padrão)*/
        public static final String VUEJS = "http://localhost:5173";

        /** Frontend Angular (porta padrão) */
        public static final String ANGULAR = "http://localhost:4200";

        /** Própria API (para testes) */
        public static final String SELF = "http://localhost:8080";

        public static final String EXPO_WEB = "http://localhost:8081";


        /**
         * Lista de todas as origens permitidas.
         * <p>
         * Em produção, substituir por:
         * - https://shoppinglist.com.br
         */
        public static List<String> getAll() {
            return List.of(REACT, ANGULAR, SELF, VUEJS, EXPO_WEB);
        }

        private AllowedOrigins() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /**
     * Métodos HTTP permitidos.
     */
    public static final class AllowedMethods {

        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String PATCH = "PATCH";
        public static final String OPTIONS = "OPTIONS";

        /**
         * Lista de todos os métodos permitidos.
         */
        public static List<String> getAll() {
            return List.of(GET, POST, PUT, DELETE, PATCH, OPTIONS);
        }

        private AllowedMethods() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /**
     * Headers permitidos nas requisições.
     */
    public static final class AllowedHeaders {

        /** Token JWT de autenticação */
        public static final String AUTHORIZATION = "Authorization";

        /** Tipo do conteúdo enviado */
        public static final String CONTENT_TYPE = "Content-Type";

        /** Identifica requisições AJAX */
        public static final String X_REQUESTED_WITH = "X-Requested-With";

        /** Tipos de resposta aceitos */
        public static final String ACCEPT = "Accept";

        /** Origem da requisição */
        public static final String ORIGIN = "Origin";

        /** Método da requisição preflight */
        public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

        /** Headers da requisição preflight */
        public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

        /**
         * Lista de todos os headers permitidos.
         */
        public static List<String> getAll() {
            return List.of(
                    AUTHORIZATION,
                    CONTENT_TYPE,
                    X_REQUESTED_WITH,
                    ACCEPT,
                    ORIGIN,
                    ACCESS_CONTROL_REQUEST_METHOD,
                    ACCESS_CONTROL_REQUEST_HEADERS
            );
        }

        private AllowedHeaders() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /**
     * Headers expostos nas respostas.
     */
    public static final class ExposedHeaders {

        /** Token de autenticação na resposta */
        public static final String AUTHORIZATION = "Authorization";

        /** Origem permitida */
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

        /** Permite credenciais */
        public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

        /**
         * Lista de todos os headers expostos.
         */
        public static List<String> getAll() {
            return List.of(
                    AUTHORIZATION,
                    ACCESS_CONTROL_ALLOW_ORIGIN,
                    ACCESS_CONTROL_ALLOW_CREDENTIALS
            );
        }

        private ExposedHeaders() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    /**
     * Outras configurações CORS.
     */
    public static final class Config {

        /** Permite envio de credenciais (cookies, headers de autenticação) */
        public static final boolean ALLOW_CREDENTIALS = true;

        /** Tempo de cache da resposta preflight (1 hora em segundos) */
        public static final long MAX_AGE_SECONDS = 3600L;

        private Config() {
            throw new UnsupportedOperationException("Utility class");
        }
    }
}


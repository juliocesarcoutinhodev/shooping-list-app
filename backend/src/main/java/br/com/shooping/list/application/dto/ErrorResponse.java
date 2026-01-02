package br.com.shooping.list.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

/**
 * DTO padronizado para respostas de erro da API.
 * <p>
 * Segue o padrão RFC 7807 (Problem Details for HTTP APIs).
 * <p>
 * Usado por {@link br.com.shooping.list.infrastructure.exception.GlobalExceptionHandler}
 * para retornar erros consistentes em toda a API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        /**
         * Timestamp do erro (ISO-8601).
         */
        Instant timestamp,

        /**
         * Código HTTP do erro (400, 401, 404, 500, etc).
         */
        int status,

        /**
         * Nome do erro (Bad Request, Unauthorized, Not Found, etc).
         */
        String error,

        /**
         * Mensagem descritiva do erro.
         */
        String message,

        /**
         * Path da requisição que gerou o erro.
         */
        String path,

        /**
         * Detalhes adicionais do erro (opcional).
         * Útil para erros de validação com múltiplos campos.
         */
        List<ValidationError> details,

        /**
         * Correlation ID para rastreamento distribuído.
         * Permite correlacionar logs e requisições através de toda a stack.
         * Útil em ambientes de produção para debugging e auditoria.
         */
        String correlationId
) {
    /**
     * Representa um erro de validação de campo específico.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ValidationError(
            /**
             * Nome do campo com erro.
             */
            String field,

            /**
             * Mensagem de erro do campo.
             */
            String message,

            /**
             * Valor rejeitado (opcional).
             */
            Object rejectedValue
    ) {}

    /**
     * Factory method para criar erro simples sem detalhes.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                null,
                MDC.get("correlationId")
        );
    }

    /**
     * Factory method para criar erro com detalhes de validação.
     */
    public static ErrorResponse withDetails(
            int status,
            String error,
            String message,
            String path,
            List<ValidationError> details
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                details,
                MDC.get("correlationId")
        );
    }
}


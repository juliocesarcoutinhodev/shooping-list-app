package br.com.shooping.list.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
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
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp do erro (ISO-8601).
     */
    private final Instant timestamp;

    /**
     * Código HTTP do erro (400, 401, 404, 500, etc).
     */
    private final int status;

    /**
     * Nome do erro (Bad Request, Unauthorized, Not Found, etc).
     */
    private final String error;

    /**
     * Mensagem descritiva do erro.
     */
    private final String message;

    /**
     * Path da requisição que gerou o erro.
     */
    private final String path;

    /**
     * Detalhes adicionais do erro (opcional).
     * Útil para erros de validação com múltiplos campos.
     */
    private final List<ValidationError> details;

    /**
     * Correlation ID para rastreamento distribuído.
     * Permite correlacionar logs e requisições através de toda a stack.
     * Útil em ambientes de produção para debugging e auditoria.
     */
    private final String correlationId;

    /**
     * Representa um erro de validação de campo específico.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {

        /**
         * Nome do campo com erro.
         */
        private final String field;

        /**
         * Mensagem de erro do campo.
         */
        private final String message;

        /**
         * Valor rejeitado (opcional).
         */
        private final Object rejectedValue;
    }

    /**
     * Factory method para criar erro simples sem detalhes.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .correlationId(MDC.get("correlationId"))
                .build();
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
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .details(details)
                .correlationId(MDC.get("correlationId"))
                .build();
    }
}


package br.com.shooping.list.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    name = "ErrorResponse",
    description = "Standardized error response following RFC 7807 (Problem Details for HTTP APIs)"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(
            description = "Error occurrence timestamp (ISO-8601 UTC)",
            example = "2026-01-02T15:30:45.123Z",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant timestamp,

        @Schema(
            description = "HTTP status code",
            example = "400",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        int status,

        @Schema(
            description = "HTTP status text",
            example = "Bad Request",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String error,

        @Schema(
            description = "Human-readable error message",
            example = "Validation failed for request body",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String message,

        @Schema(
            description = "Request path that caused the error",
            example = "/api/v1/auth/register",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String path,

        @Schema(
            description = "Detailed validation errors (present only for 400 Bad Request with field validations)",
            nullable = true,
            accessMode = Schema.AccessMode.READ_ONLY
        )
        List<ValidationError> details,

        @Schema(
            description = "Correlation ID for distributed tracing (useful for debugging in production logs)",
            example = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
            nullable = true,
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String correlationId
) {
    /**
     * Representa um erro de validação de campo específico.
     */
    @Schema(
        name = "ErrorValidationError",
        description = "Field-level validation error details"
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ValidationError(
            @Schema(
                description = "Field name that failed validation",
                example = "email",
                accessMode = Schema.AccessMode.READ_ONLY
            )
            String field,

            @Schema(
                description = "Validation error message",
                example = "Email é obrigatório",
                accessMode = Schema.AccessMode.READ_ONLY
            )
            String message,

            @Schema(
                description = "Value that was rejected (optional, may be null for security)",
                example = "invalid-email",
                nullable = true,
                accessMode = Schema.AccessMode.READ_ONLY
            )
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


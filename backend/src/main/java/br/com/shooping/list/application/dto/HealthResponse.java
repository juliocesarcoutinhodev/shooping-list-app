package br.com.shooping.list.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
    name = "HealthResponse",
    description = "API health check response"
)
public record HealthResponse(
        @Schema(
            description = "API health status",
            example = "UP",
            allowableValues = {"UP", "DOWN"},
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String status
) {}

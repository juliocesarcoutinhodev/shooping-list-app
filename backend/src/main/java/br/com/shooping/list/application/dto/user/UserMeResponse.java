package br.com.shooping.list.application.dto.user;

import br.com.shooping.list.domain.user.AuthProvider;
import br.com.shooping.list.domain.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO de resposta com dados do usuário autenticado.
 * Retornado pelo endpoint GET /api/v1/users/me
 */
@Schema(
    name = "UserMeResponse",
    description = "Authenticated user profile data (no sensitive information)"
)
public record UserMeResponse(
        @Schema(
            description = "User unique identifier",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
            description = "User email address",
            example = "user@example.com",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String email,

        @Schema(
            description = "User full name",
            example = "John Doe",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        String name,

        @Schema(
            description = "Authentication provider (LOCAL or GOOGLE)",
            example = "LOCAL",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        AuthProvider provider,

        @Schema(
            description = "User account status (ACTIVE, INACTIVE, BLOCKED)",
            example = "ACTIVE",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        UserStatus status,

        @Schema(
            description = "Account creation timestamp (ISO-8601 UTC)",
            example = "2026-01-01T10:00:00.000Z",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant createdAt,

        @Schema(
            description = "Last update timestamp (ISO-8601 UTC)",
            example = "2026-01-02T15:30:00.000Z",
            accessMode = Schema.AccessMode.READ_ONLY
        )
        Instant updatedAt
) {}


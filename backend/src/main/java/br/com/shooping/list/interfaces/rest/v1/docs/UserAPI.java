package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.ErrorResponse;
import br.com.shooping.list.application.dto.user.UserMeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * OpenAPI documentation contract for authenticated user endpoints.
 */
@Tag(
        name = "User",
        description = """
                Endpoints for managing the authenticated user's profile.

                Capabilities:
                - Retrieve current user profile
                - Update personal information (future)
                - Delete account (future)

                All endpoints require JWT (Bearer).
                """
)
public interface UserAPI {

    @Operation(
            summary = "Get current authenticated user",
            description = """
                    Returns profile information for the currently authenticated user.

                    Returned data:
                    - ID, email, and name
                    - Authentication provider (LOCAL or GOOGLE)
                    - Account status (ACTIVE, INACTIVE, BLOCKED)
                    - Creation and update timestamps

                    Security notes:
                    - User identity is resolved from the JWT
                    - No sensitive data is exposed (passwords, tokens, credentials)
                    - Depending on implementation, data may be enriched from persistence

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserMeResponse.class),
                            examples = @ExampleObject(
                                    name = "LOCAL user",
                                    value = """
                                            {
                                              "id": 1,
                                              "email": "user@example.com",
                                              "name": "John Doe",
                                              "provider": "LOCAL",
                                              "status": "ACTIVE",
                                              "createdAt": "2026-01-01T10:00:00Z",
                                              "updatedAt": "2026-01-02T15:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated (missing or invalid token)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Unauthenticated.",
                                              "path": "/api/v1/users/me",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found (valid token but user no longer exists)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Not found",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "User not found.",
                                              "path": "/api/v1/users/me",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<UserMeResponse> getCurrentUser();
}

package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.ErrorResponse;
import br.com.shooping.list.application.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenAPI documentation contract for authentication endpoints.
 *
 * <p>This interface isolates documentation concerns from controller implementation,
 * keeping controllers focused on orchestration and avoiding annotation noise.
 */
@Tag(
        name = "Authentication",
        description = """
                Authentication and authorization endpoints.

                Capabilities:
                - User registration (LOCAL)
                - Email/password login
                - Google OAuth2 login
                - Token refresh
                - Logout and token revocation

                Security:
                - JWT access tokens with configurable expiration
                - Refresh tokens stored in HttpOnly cookies
                - Refresh token rotation support
                """
)
public interface AuthAPI {

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new LOCAL user account using email and password.

                    Business rules:
                    - Email must be unique
                    - Password must be at least 8 characters
                    - Password is stored using BCrypt hashing
                    - USER role is assigned by default

                    Public endpoint: does not require JWT authentication.
                    """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "User created",
                                    value = """
                                            {
                                              "id": 1,
                                              "email": "user@example.com",
                                              "name": "John Doe",
                                              "provider": "LOCAL",
                                              "status": "ACTIVE",
                                              "createdAt": "2026-01-02T10:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (validation failed)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid input (validation failed).",
                                              "path": "/api/v1/auth/register",
                                              "details": [
                                                {
                                                  "field": "email",
                                                  "message": "Email deve ser válido",
                                                  "rejectedValue": "invalid-email"
                                                }
                                              ],
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already registered",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Conflict",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Email already registered.",
                                              "path": "/api/v1/auth/register",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(
            summary = "Login with email and password",
            description = """
                    Authenticates a LOCAL user using email and password.

                    Returns:
                    - Access token (JWT)
                    - Refresh token (also set as HttpOnly cookie)

                    Security notes:
                    - Password is verified using BCrypt
                    - Tokens may be issued with device context (user-agent, IP) if enabled

                    Public endpoint: does not require JWT authentication.
                    """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "JWT tokens",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "expiresIn": 900
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (validation failed)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid input (validation failed).",
                                              "path": "/api/v1/auth/login",
                                              "details": [
                                                {
                                                  "field": "email",
                                                  "message": "Email deve ser válido",
                                                  "rejectedValue": "invalid-email"
                                                }
                                              ],
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Invalid credentials.",
                                              "path": "/api/v1/auth/login",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "Login with Google OAuth2",
            description = """
                    Authenticates a user using Google OAuth2 ID Token.

                    Flow:
                    1. Client obtains an ID Token from Google
                    2. Client sends the ID Token to this endpoint
                    3. Backend validates the token with Google
                    4. User is created or updated
                    5. Backend returns first-party JWT tokens

                    Public endpoint: does not require JWT authentication.
                    """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Google login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "JWT tokens",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "expiresIn": 900
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ID token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad request",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid ID token.",
                                              "path": "/api/v1/auth/google",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Google token invalid or expired",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Google token invalid or expired.",
                                              "path": "/api/v1/auth/google",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "Refresh access token",
            description = """
                    Issues a new access token using a valid refresh token.

                    Refresh token sources:
                    - HttpOnly cookie (recommended)
                    - Request body (fallback for dev/test)

                    Rotation:
                    - A new refresh token is issued
                    - The previous refresh token is invalidated

                    Public endpoint: does not require JWT authentication (uses refresh token instead).
                    """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenResponse.class),
                            examples = @ExampleObject(
                                    name = "New tokens",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "expiresIn": 900
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token not provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad request",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Refresh token not provided.",
                                              "path": "/api/v1/auth/refresh",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalid, expired or revoked",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Refresh token invalid, expired or revoked.",
                                              "path": "/api/v1/auth/refresh",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<RefreshTokenResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "Logout (revoke session)",
            description = """
                    Revokes the current refresh token and invalidates the session.

                    Effects:
                    - Refresh token revoked/removed from persistence
                    - Refresh cookie cleared (if applicable)
                    - Access token remains valid until expiration (stateless JWT)

                    Requires JWT authentication (valid access token).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful (no content)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token not provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad request",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Refresh token not provided.",
                                              "path": "/api/v1/auth/logout",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated (invalid access token)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:30:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Unauthenticated (invalid access token).",
                                              "path": "/api/v1/auth/logout",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );
}

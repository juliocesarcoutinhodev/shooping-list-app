package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.ErrorResponse;
import br.com.shooping.list.application.dto.shoppinglist.AddItemRequest;
import br.com.shooping.list.application.dto.shoppinglist.ItemResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateItemRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenAPI documentation contract for Shopping List Items endpoints.
 */
@Tag(
        name = "Shopping List Items",
        description = """
                Endpoints for managing items within shopping lists.

                Capabilities:
                - Add items to a list
                - Update items (name, quantity, unit, unit price, status)
                - Toggle item status (PENDING / PURCHASED)
                - Remove items

                Business rules:
                - Maximum of 100 items per list
                - Item names must be unique within the same list
                - Default status: PENDING
                """
)
public interface ShoppingListItemAPI {

    @Operation(
            summary = "Add an item to a shopping list",
            description = """
                    Adds a new item to an existing shopping list.

                    Required fields:
                    - name (2-100 chars, must be unique within the list)
                    - quantity (must be greater than 0)
                    - unit (e.g., kg, un, L)

                    Optional fields:
                    - unitPrice (unit price for estimated total calculations)

                    Validations and rules:
                    - The list must exist
                    - Only the list owner can add items
                    - Item name must be unique (case/whitespace normalized)
                    - List must not exceed 100 items

                    Behavior:
                    - Initial status: PENDING
                    - Name may be normalized to detect duplicates reliably

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Item created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class),
                            examples = @ExampleObject(
                                    name = "Created item",
                                    value = """
                                            {
                                              "id": 1,
                                              "name": "Rice",
                                              "quantity": 2.0,
                                              "unit": "kg",
                                              "unitPrice": 5.50,
                                              "status": "PENDING",
                                              "createdAt": "2026-01-02T10:05:00Z",
                                              "updatedAt": "2026-01-02T10:05:00Z"
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
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid input (validation failed).",
                                              "path": "/api/v1/lists/1/items",
                                              "details": [
                                                {
                                                  "field": "name",
                                                  "message": "Nome é obrigatório",
                                                  "rejectedValue": ""
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
                    description = "Unauthenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Unauthenticated.",
                                              "path": "/api/v1/lists/1/items",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the list owner)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Forbidden (not the list owner).",
                                              "path": "/api/v1/lists/1/items",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shopping list not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Not found",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Shopping list not found.",
                                              "path": "/api/v1/lists/999/items",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Item name already exists within the list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Conflict",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Item name already exists within the list.",
                                              "path": "/api/v1/lists/1/items",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "List item limit reached (max 100 items)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unprocessable entity",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T10:05:00Z",
                                              "status": 422,
                                              "error": "Unprocessable Entity",
                                              "message": "List item limit reached (max 100 items).",
                                              "path": "/api/v1/lists/1/items",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ItemResponse> addItemToList(
            @Parameter(
                    name = "listId",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long listId,
            @Valid @RequestBody AddItemRequest request
    );

    @Operation(
            summary = "Update a shopping list item",
            description = """
                    Updates an existing item.

                    Partial update:
                    - Send only the fields you want to change
                    - Omitted fields remain unchanged

                    Updatable fields:
                    - name (must remain unique within the list)
                    - quantity (must be greater than 0)
                    - unit (e.g., kg, un, L)
                    - unitPrice (unit price)
                    - status (PENDING or PURCHASED)

                    Common usage:
                    - Toggle status: send {"status": "PURCHASED"} or {"status": "PENDING"}
                    - Update quantity: send {"quantity": 3.0}

                    Access rules:
                    - The list must exist
                    - The item must exist within the list
                    - Only the owner can update items
                    - If name changes, it must remain unique

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemResponse.class),
                            examples = @ExampleObject(
                                    name = "Updated item",
                                    value = """
                                            {
                                              "id": 1,
                                              "name": "Brown Rice",
                                              "quantity": 3.0,
                                              "unit": "kg",
                                              "unitPrice": 6.00,
                                              "status": "PURCHASED",
                                              "createdAt": "2026-01-02T10:05:00Z",
                                              "updatedAt": "2026-01-02T15:30:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or no fields provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Bad request",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid input or no fields provided.",
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
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
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the list owner)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Forbidden (not the list owner).",
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shopping list or item not found",
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
                                              "message": "Shopping list or item not found.",
                                              "path": "/api/v1/lists/999/items/999",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Item name already exists within the list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Conflict",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Item name already exists within the list.",
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ItemResponse> updateItem(
            @Parameter(
                    name = "listId",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long listId,
            @Parameter(
                    name = "itemId",
                    description = "Item ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request
    );

    @Operation(
            summary = "Remove an item from a shopping list",
            description = """
                    Permanently removes an item from a shopping list.

                    Notes:
                    - This operation is irreversible

                    Access rules:
                    - The list must exist
                    - The item must exist within the list
                    - Only the owner can remove items

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Item removed successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
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
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the list owner)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Forbidden",
                                    value = """
                                            {
                                              "timestamp": "2026-01-02T15:30:00Z",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Forbidden (not the list owner).",
                                              "path": "/api/v1/lists/1/items/1",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shopping list or item not found",
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
                                              "message": "Shopping list or item not found.",
                                              "path": "/api/v1/lists/999/items/999",
                                              "correlationId": "c2f1b2aa6e9f4f4a9df2d7c7b1e4d7a1"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> removeItem(
            @Parameter(
                    name = "listId",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long listId,
            @Parameter(
                    name = "itemId",
                    description = "Item ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long itemId
    );
}

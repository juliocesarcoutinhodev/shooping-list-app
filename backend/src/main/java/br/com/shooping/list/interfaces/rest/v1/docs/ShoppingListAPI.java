package br.com.shooping.list.interfaces.rest.v1.docs;

import br.com.shooping.list.application.dto.shoppinglist.CreateShoppingListRequest;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListResponse;
import br.com.shooping.list.application.dto.shoppinglist.ShoppingListSummaryResponse;
import br.com.shooping.list.application.dto.shoppinglist.UpdateShoppingListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

/**
 * OpenAPI documentation contract for Shopping Lists endpoints.
 */
@Tag(
        name = "Shopping Lists",
        description = """
                CRUD endpoints for managing shopping lists.

                Capabilities:
                - Create new lists
                - List all user lists (summary view)
                - Get list details (including items)
                - Update title and description
                - Delete a list (cascade delete items)

                Business rules:
                - Each list belongs to a single user (owner)
                - Only the owner can view/modify/delete the list
                - Max 100 items per list
                """
)
public interface ShoppingListAPI {

    @Operation(
            summary = "Create a shopping list",
            description = """
                    Creates a new shopping list for the authenticated user.

                    Required fields:
                    - title (2-100 chars)

                    Optional fields:
                    - description (up to 500 chars)

                    Behavior:
                    - List is created empty (no items)
                    - Owner is extracted from the JWT
                    - ID is generated automatically

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "List created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShoppingListResponse.class),
                            examples = @ExampleObject(
                                    name = "Created list",
                                    value = """
                                            {
                                              "id": 1,
                                              "ownerId": 1,
                                              "title": "Monthly groceries",
                                              "description": "Supermarket shopping list",
                                              "items": null,
                                              "itemsCount": 0,
                                              "pendingItemsCount": 0,
                                              "purchasedItemsCount": 0,
                                              "createdAt": "2026-01-02T10:00:00Z",
                                              "updatedAt": "2026-01-02T10:00:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input (validation failed)",
                    content = @Content(mediaType = "application/json")
                    // TODO: ValidationErrorResponse schema
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(mediaType = "application/json")
                    // TODO: ErrorResponse schema
            )
    })
    ResponseEntity<ShoppingListResponse> createList(@Valid @RequestBody CreateShoppingListRequest request);

    @Operation(
            summary = "List my shopping lists (summary)",
            description = """
                    Returns a summary view of all shopping lists owned by the authenticated user.

                    Response characteristics:
                    - Summary view (items are not included)
                    - Sorted by creation date (newest first)
                    - Includes counters: total items, pending items, purchased items

                    For full details, use GET /lists/{id}.

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lists returned successfully (may be empty)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ShoppingListSummaryResponse.class)),
                            examples = @ExampleObject(
                                    name = "Summary list",
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "title": "Monthly groceries",
                                                "itemsCount": 5,
                                                "pendingItemsCount": 3,
                                                "createdAt": "2026-01-02T10:00:00Z",
                                                "updatedAt": "2026-01-02T15:30:00Z"
                                              },
                                              {
                                                "id": 2,
                                                "title": "Pharmacy",
                                                "itemsCount": 2,
                                                "pendingItemsCount": 2,
                                                "createdAt": "2026-01-01T08:00:00Z",
                                                "updatedAt": "2026-01-01T08:00:00Z"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(mediaType = "application/json")
                    // TODO: ErrorResponse schema
            )
    })
    ResponseEntity<List<ShoppingListSummaryResponse>> getMyLists();

    @Operation(
            summary = "Get shopping list details",
            description = """
                    Returns full details of a specific shopping list, including all items.

                    Returned data:
                    - List metadata (title, description, counters)
                    - All items with status (PENDING or PURCHASED)

                    Access rules:
                    - The list must exist
                    - Only the owner can access it

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShoppingListResponse.class),
                            examples = @ExampleObject(
                                    name = "Full list",
                                    value = """
                                            {
                                              "id": 1,
                                              "ownerId": 1,
                                              "title": "Monthly groceries",
                                              "description": "Supermarket shopping list",
                                              "items": [
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
                                              ],
                                              "itemsCount": 1,
                                              "pendingItemsCount": 1,
                                              "purchasedItemsCount": 0,
                                              "createdAt": "2026-01-02T10:00:00Z",
                                              "updatedAt": "2026-01-02T10:05:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the list owner)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "List not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    ResponseEntity<ShoppingListResponse> getListById(
            @Parameter(
                    name = "id",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    );

    @Operation(
            summary = "Update a shopping list",
            description = """
                    Updates the title and/or description of an existing shopping list.

                    Partial update:
                    - Send only the fields you want to change
                    - Omitted fields remain unchanged
                    - To clear the description, send an empty string

                    Access rules:
                    - The list must exist
                    - Only the owner can update it
                    - At least one field must be provided

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ShoppingListResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or no fields provided",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the owner)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "List not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    ResponseEntity<ShoppingListResponse> updateList(
            @Parameter(
                    name = "id",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody UpdateShoppingListRequest request
    );

    @Operation(
            summary = "Delete a shopping list",
            description = """
                    Permanently deletes a shopping list and all its items.

                    Notes:
                    - This operation is irreversible
                    - Items are deleted as part of the cascade

                    Access rules:
                    - The list must exist
                    - Only the owner can delete it

                    Requires JWT (Bearer).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "List deleted successfully (no content)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthenticated",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (not the owner)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "List not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    ResponseEntity<Void> deleteList(
            @Parameter(
                    name = "id",
                    description = "Shopping list ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    );
}

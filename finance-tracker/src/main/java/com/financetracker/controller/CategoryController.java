package com.financetracker.controller;

import com.financetracker.dto.category.CategoryRequest;
import com.financetracker.dto.category.CategoryResponse;
import com.financetracker.model.User;
import com.financetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for transaction category management.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Transaction category management")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Constructs the CategoryController with the CategoryService dependency.
     *
     * @param categoryService the category management service
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Returns all categories available to the current user.
     *
     * @param authentication the current security authentication
     * @return 200 OK with list of categories
     */
    @GetMapping
    @Operation(summary = "Get all categories (system defaults and user-owned)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<CategoryResponse>> getCategories(Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(categoryService.getCategories(userId));
    }

    /**
     * Creates a new user-defined category.
     *
     * @param request the category creation request
     * @param authentication the current security authentication
     * @return 201 Created with the new category
     */
    @PostMapping
    @Operation(summary = "Create a new user-defined category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Category name already exists")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request, Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        var created = categoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Deletes a user-owned category by ID.
     *
     * @param id the category ID to delete
     * @param authentication the current security authentication
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user-owned category")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Cannot delete system-default category"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "409", description = "Category is referenced by transactions or budgets")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id,
                                               Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }
}

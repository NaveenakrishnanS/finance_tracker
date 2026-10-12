package com.financetracker.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a user-defined category.
 *
 * @param name the category name (required, max 100 characters)
 */
@Schema(description = "Request to create a custom transaction category")
public record CategoryRequest(
        @Schema(description = "Category name (unique per user, max 100 characters)", example = "Gym")
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name
) {}

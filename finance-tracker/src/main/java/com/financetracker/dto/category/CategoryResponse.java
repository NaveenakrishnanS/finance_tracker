package com.financetracker.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body representing a transaction category.
 *
 * @param id the category's unique identifier
 * @param name the category name
 * @param isSystemDefault whether this is a system-provided default category
 */
@Schema(description = "Transaction category")
public record CategoryResponse(
        @Schema(description = "Unique category ID", example = "3")
        Long id,

        @Schema(description = "Category name", example = "Food & Dining")
        String name,

        @Schema(description = "True if this is a system-provided default category that cannot be deleted", example = "true")
        boolean isSystemDefault
) {}

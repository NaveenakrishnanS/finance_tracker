package com.financetracker.dto.budget;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO for a budget, including computed spending information.
 *
 * @param id the budget ID
 * @param categoryId the category ID this budget applies to
 * @param categoryName the name of the category
 * @param limitAmount the monthly spending limit
 * @param month the month (1-12) this budget applies to
 * @param year the year this budget applies to
 * @param spentAmount the total expenses recorded for this category and period
 * @param overLimit true if spentAmount exceeds limitAmount
 */
@Schema(description = "Budget response including computed spending information")
public record BudgetResponse(

        @Schema(description = "Unique budget identifier", example = "1")
        Long id,

        @Schema(description = "Category ID this budget applies to", example = "2")
        Long categoryId,

        @Schema(description = "Name of the category", example = "Food")
        String categoryName,

        @Schema(description = "Monthly spending limit", example = "500.0000")
        BigDecimal limitAmount,

        @Schema(description = "Month (1-12) this budget applies to", example = "3")
        int month,

        @Schema(description = "Year this budget applies to", example = "2026")
        int year,

        @Schema(description = "Total expenses recorded for this category and period", example = "350.0000")
        BigDecimal spentAmount,

        @Schema(description = "True if spentAmount exceeds limitAmount", example = "false")
        boolean overLimit
) {}

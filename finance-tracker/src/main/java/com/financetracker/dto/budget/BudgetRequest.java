package com.financetracker.dto.budget;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a budget.
 *
 * @param categoryId the ID of the category to budget for
 * @param limitAmount the spending limit amount
 * @param month the month (1-12) the budget applies to
 * @param year the year the budget applies to
 */
@Schema(description = "Request body for creating or updating a budget")
public record BudgetRequest(

        @NotNull(message = "Category ID is required")
        @Schema(description = "ID of the category to budget for", example = "1")
        Long categoryId,

        @NotNull(message = "Limit amount is required")
        @DecimalMin(value = "0.01", message = "Limit amount must be at least 0.01")
        @Schema(description = "Spending limit for the budget period", example = "500.00")
        BigDecimal limitAmount,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        @Schema(description = "Month (1-12) the budget applies to", example = "3")
        Integer month,

        @NotNull(message = "Year is required")
        @Schema(description = "Year the budget applies to", example = "2026")
        Integer year
) {}

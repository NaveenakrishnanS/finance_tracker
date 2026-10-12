package com.financetracker.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Per-category breakdown of income and expenses for a report summary.
 *
 * @param categoryId the category identifier
 * @param categoryName the category name
 * @param income total income for this category in the period
 * @param expense total expenses for this category in the period
 */
@Schema(description = "Income and expense breakdown for a specific category")
public record CategoryBreakdown(

        @Schema(description = "Category identifier", example = "1")
        Long categoryId,

        @Schema(description = "Category name", example = "Food")
        String categoryName,

        @Schema(description = "Total income for this category in the period", example = "0.0000")
        BigDecimal income,

        @Schema(description = "Total expenses for this category in the period", example = "350.0000")
        BigDecimal expense
) {}

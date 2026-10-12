package com.financetracker.dto.transaction;

import com.financetracker.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response body representing a financial transaction.
 *
 * @param id the transaction's unique identifier
 * @param amount the transaction amount
 * @param type the transaction type (INCOME or EXPENSE)
 * @param categoryId the category's unique identifier
 * @param categoryName the category name
 * @param date the transaction date
 * @param description the optional description
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
@Schema(description = "Financial transaction")
public record TransactionResponse(
        @Schema(description = "Unique transaction ID", example = "101")
        Long id,

        @Schema(description = "Transaction amount", example = "49.99")
        BigDecimal amount,

        @Schema(description = "Transaction type", example = "EXPENSE")
        TransactionType type,

        @Schema(description = "Category ID", example = "3")
        Long categoryId,

        @Schema(description = "Category name", example = "Food & Dining")
        String categoryName,

        @Schema(description = "Transaction date (yyyy-MM-dd)", example = "2026-03-10")
        LocalDate date,

        @Schema(description = "Optional description", example = "Weekly grocery run")
        String description,

        @Schema(description = "Record creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Record last-updated timestamp")
        LocalDateTime updatedAt
) {}

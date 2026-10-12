package com.financetracker.dto.transaction;

import com.financetracker.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for creating or updating a transaction.
 *
 * @param amount the transaction amount (must be at least 0.01)
 * @param type the transaction type (INCOME or EXPENSE)
 * @param categoryId the ID of the category for this transaction
 * @param date the date of the transaction
 * @param description optional description (max 500 characters)
 */
@Schema(description = "Request to create or update a financial transaction")
public record TransactionRequest(
        @Schema(description = "Transaction amount (minimum 0.01)", example = "49.99")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @Schema(description = "ID of the category to assign to this transaction", example = "3")
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @Schema(description = "Date of the transaction (yyyy-MM-dd)", example = "2026-03-10")
        @NotNull(message = "Date is required")
        LocalDate date,

        @Schema(description = "Optional description (max 500 characters)", example = "Weekly grocery run")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {}

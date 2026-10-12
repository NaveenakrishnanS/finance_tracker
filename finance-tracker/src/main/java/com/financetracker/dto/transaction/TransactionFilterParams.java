package com.financetracker.dto.transaction;

import com.financetracker.model.TransactionType;
import java.time.LocalDate;

/**
 * Optional filter parameters for the transaction list endpoint.
 *
 * @param type optional filter by transaction type (INCOME or EXPENSE)
 * @param categoryId optional filter by category ID
 * @param startDate optional start date for date range filter
 * @param endDate optional end date for date range filter
 */
public record TransactionFilterParams(
        TransactionType type,
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate
) {}

package com.financetracker.service;

import com.financetracker.dto.report.CategoryBreakdown;
import com.financetracker.dto.report.MonthlyTrendResponse;
import com.financetracker.dto.report.SummaryResponse;
import com.financetracker.model.TransactionType;
import com.financetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Service for generating financial reports.
 */
@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    /**
     * Constructs ReportService with required dependencies.
     *
     * @param transactionRepository the transaction data repository used for aggregation queries
     */
    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns a financial summary for the given user and date range.
     * If startDate or endDate is null, defaults to the first and last day of the current calendar month.
     *
     * @param userId the user ID
     * @param startDate optional start date; defaults to first day of current month
     * @param endDate optional end date; defaults to last day of current month
     * @return the summary response with totals and per-category breakdown
     */
    public SummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStart = startDate != null ? startDate : currentMonthStart();
        LocalDate effectiveEnd = endDate != null ? endDate : currentMonthEnd();

        List<Object[]> rows = transactionRepository.findSummaryByCategory(
                userId, effectiveStart, effectiveEnd);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        Map<Long, CategoryBreakdown> breakdownMap = new HashMap<>();

        for (Object[] row : rows) {
            Long categoryId = ((Number) row[0]).longValue();
            String categoryName = (String) row[1];
            TransactionType type = (row[2] instanceof TransactionType)
                    ? (TransactionType) row[2]
                    : TransactionType.valueOf(row[2].toString());
            BigDecimal amount = (BigDecimal) row[3];

            if (amount == null) {
                amount = BigDecimal.ZERO;
            }

            CategoryBreakdown existing = breakdownMap.getOrDefault(categoryId,
                    new CategoryBreakdown(categoryId, categoryName,
                            BigDecimal.ZERO, BigDecimal.ZERO));

            if (type == TransactionType.INCOME) {
                totalIncome = totalIncome.add(amount).setScale(4, RoundingMode.HALF_UP);
                existing = new CategoryBreakdown(categoryId, categoryName,
                        existing.income().add(amount), existing.expense());
            } else {
                totalExpenses = totalExpenses.add(amount).setScale(4, RoundingMode.HALF_UP);
                existing = new CategoryBreakdown(categoryId, categoryName,
                        existing.income(), existing.expense().add(amount));
            }
            breakdownMap.put(categoryId, existing);
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpenses)
                .setScale(4, RoundingMode.HALF_UP);

        return new SummaryResponse(effectiveStart, effectiveEnd,
                totalIncome, totalExpenses, netBalance,
                new ArrayList<>(breakdownMap.values()));
    }

    /**
     * Returns monthly trend data for the given user and date range, ordered by year and month ascending.
     * If startDate or endDate is null, defaults to the first and last day of the current calendar month.
     *
     * @param userId the user ID
     * @param startDate optional start date; defaults to first day of current month
     * @param endDate optional end date; defaults to last day of current month
     * @return list of monthly trend responses ordered by year and month ascending
     */
    public List<MonthlyTrendResponse> getMonthlyTrend(Long userId,
                                                        LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveStart = startDate != null ? startDate : currentMonthStart();
        LocalDate effectiveEnd = endDate != null ? endDate : currentMonthEnd();

        List<Object[]> rows = transactionRepository.findMonthlyTrend(
                userId, effectiveStart, effectiveEnd);

        Map<String, MonthlyTrendResponse> monthMap = new HashMap<>();

        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = (row[2] instanceof TransactionType)
                    ? (TransactionType) row[2]
                    : TransactionType.valueOf(row[2].toString());
            BigDecimal amount = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;

            String key = year + "-" + month;
            MonthlyTrendResponse existing = monthMap.getOrDefault(key,
                    new MonthlyTrendResponse(month, year,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

            if (type == TransactionType.INCOME) {
                BigDecimal newIncome = existing.getTotalIncome().add(amount);
                BigDecimal newNet = newIncome.subtract(existing.getTotalExpenses())
                        .setScale(4, RoundingMode.HALF_UP);
                monthMap.put(key, new MonthlyTrendResponse(month, year,
                        newIncome, existing.getTotalExpenses(), newNet));
            } else {
                BigDecimal newExpenses = existing.getTotalExpenses().add(amount);
                BigDecimal newNet = existing.getTotalIncome().subtract(newExpenses)
                        .setScale(4, RoundingMode.HALF_UP);
                monthMap.put(key, new MonthlyTrendResponse(month, year,
                        existing.getTotalIncome(), newExpenses, newNet));
            }
        }

        return monthMap.values().stream()
                .sorted((a, b) -> {
                    int yearCmp = Integer.compare(a.getYear(), b.getYear());
                    return yearCmp != 0 ? yearCmp : Integer.compare(a.getMonth(), b.getMonth());
                })
                .toList();
    }

    private LocalDate currentMonthStart() {
        YearMonth current = YearMonth.now();
        return current.atDay(1);
    }

    private LocalDate currentMonthEnd() {
        YearMonth current = YearMonth.now();
        return current.atEndOfMonth();
    }
}

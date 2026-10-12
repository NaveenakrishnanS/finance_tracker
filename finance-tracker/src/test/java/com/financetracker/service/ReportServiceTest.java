package com.financetracker.service;

import com.financetracker.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportService using JUnit 5 and Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getSummary_withExplicitDateRange_returnsCorrectTotals() {
        var start = LocalDate.of(2026, 1, 1);
        var end = LocalDate.of(2026, 1, 31);

        // row: [categoryId, categoryName, type, sum]
        when(transactionRepository.findSummaryByCategory(1L, start, end))
                .thenReturn(List.of(
                        new Object[]{1L, "Salary", "INCOME", new BigDecimal("3000.0000")},
                        new Object[]{2L, "Food", "EXPENSE", new BigDecimal("500.0000")}
                ));

        var result = reportService.getSummary(1L, start, end);

        assertThat(result.getTotalIncome().compareTo(new BigDecimal("3000.0000"))).isZero();
        assertThat(result.getTotalExpenses().compareTo(new BigDecimal("500.0000"))).isZero();
        assertThat(result.getNetBalance().compareTo(new BigDecimal("2500.0000"))).isZero();
    }

    @Test
    void getSummary_withNoDates_defaultsToCurrentCalendarMonth() {
        var currentMonth = YearMonth.now();
        var expectedStart = currentMonth.atDay(1);
        var expectedEnd = currentMonth.atEndOfMonth();

        when(transactionRepository.findSummaryByCategory(eq(1L), eq(expectedStart), eq(expectedEnd)))
                .thenReturn(List.of());

        var result = reportService.getSummary(1L, null, null);

        assertThat(result.getStartDate()).isEqualTo(expectedStart);
        assertThat(result.getEndDate()).isEqualTo(expectedEnd);
    }

    @Test
    void getSummary_categoryBreakdown_perCategoryTotalsAreCorrect() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findSummaryByCategory(1L, start, end))
                .thenReturn(List.of(
                        new Object[]{1L, "Food", "EXPENSE", new BigDecimal("200.0000")},
                        new Object[]{1L, "Food", "INCOME", new BigDecimal("50.0000")},
                        new Object[]{2L, "Transport", "EXPENSE", new BigDecimal("100.0000")}
                ));

        var result = reportService.getSummary(1L, start, end);

        assertThat(result.getCategoryBreakdowns()).hasSizeGreaterThanOrEqualTo(1);
        var foodBreakdown = result.getCategoryBreakdowns().stream()
                .filter(c -> c.categoryName().equals("Food"))
                .findFirst();
        assertThat(foodBreakdown).isPresent();
        assertThat(foodBreakdown.get().expense().compareTo(new BigDecimal("200.0000"))).isZero();
        assertThat(foodBreakdown.get().income().compareTo(new BigDecimal("50.0000"))).isZero();
    }

    @Test
    void getSummary_withNoTransactions_returnsAllZerosAndEmptyBreakdowns() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findSummaryByCategory(1L, start, end))
                .thenReturn(List.of());

        var result = reportService.getSummary(1L, start, end);

        assertThat(result.getTotalIncome().compareTo(BigDecimal.ZERO)).isZero();
        assertThat(result.getTotalExpenses().compareTo(BigDecimal.ZERO)).isZero();
        assertThat(result.getNetBalance().compareTo(BigDecimal.ZERO)).isZero();
        assertThat(result.getCategoryBreakdowns()).isEmpty();
    }

    @Test
    void getMonthlyTrend_returnsMonthsInAscendingOrder() {
        var start = LocalDate.of(2026, 1, 1);
        var end = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findMonthlyTrend(1L, start, end))
                .thenReturn(List.of(
                        new Object[]{2026, 1, "INCOME", new BigDecimal("3000.0000")},
                        new Object[]{2026, 3, "EXPENSE", new BigDecimal("500.0000")},
                        new Object[]{2026, 2, "INCOME", new BigDecimal("3000.0000")}
                ));

        var result = reportService.getMonthlyTrend(1L, start, end);

        assertThat(result).isNotEmpty();
        // Verify ascending order
        for (int i = 0; i < result.size() - 1; i++) {
            var current = result.get(i);
            var next = result.get(i + 1);
            boolean ordered = current.getYear() < next.getYear()
                    || (current.getYear() == next.getYear()
                        && current.getMonth() <= next.getMonth());
            assertThat(ordered).isTrue();
        }
    }

    @Test
    void getMonthlyTrend_eachMonthNetBalanceEqualsIncomeMinusExpenses() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findMonthlyTrend(1L, start, end))
                .thenReturn(List.of(
                        new Object[]{2026, 3, "INCOME", new BigDecimal("2000.0000")},
                        new Object[]{2026, 3, "EXPENSE", new BigDecimal("800.0000")}
                ));

        var result = reportService.getMonthlyTrend(1L, start, end);

        assertThat(result).hasSize(1);
        var march = result.get(0);
        assertThat(march.getNetBalance().compareTo(new BigDecimal("1200.0000"))).isZero();
    }

    @Test
    void getSummary_bigDecimalArithmetic_noFloatingPointErrors() {
        var start = LocalDate.of(2026, 3, 1);
        var end = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findSummaryByCategory(1L, start, end))
                .thenReturn(List.of(
                        new Object[]{1L, "Test", "INCOME", new BigDecimal("0.1000")},
                        new Object[]{1L, "Test", "EXPENSE", new BigDecimal("0.2000")}
                ));

        var result = reportService.getSummary(1L, start, end);

        // 0.1 - 0.2 = -0.1 (with BigDecimal, no floating-point error like -0.10000000000000001)
        assertThat(result.getNetBalance().compareTo(new BigDecimal("-0.1000"))).isZero();
    }
}

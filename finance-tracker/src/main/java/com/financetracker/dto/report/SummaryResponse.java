package com.financetracker.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Summary report for a given date range, including totals and per-category breakdowns.
 */
@Schema(description = "Financial summary for a date range")
public class SummaryResponse {

    @Schema(description = "Start date of the report period")
    private LocalDate startDate;

    @Schema(description = "End date of the report period")
    private LocalDate endDate;

    @Schema(description = "Total income in the period", example = "3000.0000")
    private BigDecimal totalIncome;

    @Schema(description = "Total expenses in the period", example = "1500.0000")
    private BigDecimal totalExpenses;

    @Schema(description = "Net balance (totalIncome - totalExpenses)", example = "1500.0000")
    private BigDecimal netBalance;

    @Schema(description = "Per-category income and expense breakdown")
    private List<CategoryBreakdown> categoryBreakdowns;

    /**
     * Default constructor.
     */
    public SummaryResponse() {}

    /**
     * Full constructor.
     *
     * @param startDate start of the report period
     * @param endDate end of the report period
     * @param totalIncome total income
     * @param totalExpenses total expenses
     * @param netBalance net balance
     * @param categoryBreakdowns per-category breakdown list
     */
    public SummaryResponse(LocalDate startDate, LocalDate endDate,
                            BigDecimal totalIncome, BigDecimal totalExpenses,
                            BigDecimal netBalance, List<CategoryBreakdown> categoryBreakdowns) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = netBalance;
        this.categoryBreakdowns = categoryBreakdowns;
    }

    /**
     * Returns the start date.
     *
     * @return start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date.
     *
     * @return end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns total income.
     *
     * @return total income
     */
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    /**
     * Sets total income.
     *
     * @param totalIncome total income
     */
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    /**
     * Returns total expenses.
     *
     * @return total expenses
     */
    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    /**
     * Sets total expenses.
     *
     * @param totalExpenses total expenses
     */
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    /**
     * Returns net balance.
     *
     * @return net balance
     */
    public BigDecimal getNetBalance() {
        return netBalance;
    }

    /**
     * Sets net balance.
     *
     * @param netBalance net balance
     */
    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    /**
     * Returns per-category breakdown.
     *
     * @return category breakdowns
     */
    public List<CategoryBreakdown> getCategoryBreakdowns() {
        return categoryBreakdowns;
    }

    /**
     * Sets per-category breakdown.
     *
     * @param categoryBreakdowns category breakdowns
     */
    public void setCategoryBreakdowns(List<CategoryBreakdown> categoryBreakdowns) {
        this.categoryBreakdowns = categoryBreakdowns;
    }
}

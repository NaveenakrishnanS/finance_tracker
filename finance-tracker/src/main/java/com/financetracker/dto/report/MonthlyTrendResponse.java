package com.financetracker.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Monthly financial trend data point including income, expenses, and net balance.
 */
@Schema(description = "Monthly financial trend data")
public class MonthlyTrendResponse {

    @Schema(description = "Month (1-12)", example = "3")
    private int month;

    @Schema(description = "Year", example = "2026")
    private int year;

    @Schema(description = "Total income for the month", example = "3000.0000")
    private BigDecimal totalIncome;

    @Schema(description = "Total expenses for the month", example = "1500.0000")
    private BigDecimal totalExpenses;

    @Schema(description = "Net balance (totalIncome - totalExpenses)", example = "1500.0000")
    private BigDecimal netBalance;

    /**
     * Default constructor.
     */
    public MonthlyTrendResponse() {}

    /**
     * Full constructor.
     *
     * @param month the month (1-12)
     * @param year the year
     * @param totalIncome total income for the month
     * @param totalExpenses total expenses for the month
     * @param netBalance net balance for the month
     */
    public MonthlyTrendResponse(int month, int year,
                                  BigDecimal totalIncome, BigDecimal totalExpenses,
                                  BigDecimal netBalance) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = netBalance;
    }

    /**
     * Returns the month.
     *
     * @return month (1-12)
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month.
     *
     * @param month the month (1-12)
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * Returns the year.
     *
     * @return year
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year.
     *
     * @param year the year
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns the total income.
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
     * Returns the total expenses.
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
     * Returns the net balance.
     *
     * @return net balance
     */
    public BigDecimal getNetBalance() {
        return netBalance;
    }

    /**
     * Sets the net balance.
     *
     * @param netBalance net balance
     */
    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}

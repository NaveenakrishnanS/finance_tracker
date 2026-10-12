package com.financetracker.controller;

import com.financetracker.dto.report.MonthlyTrendResponse;
import com.financetracker.dto.report.SummaryResponse;
import com.financetracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for financial reporting endpoints.
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Financial reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    /**
     * Constructs ReportController with required dependencies.
     *
     * @param reportService the report service
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Returns a financial summary for the authenticated user over the given date range.
     * If no dates are provided, defaults to the current calendar month.
     *
     * @param startDate optional start date (ISO format yyyy-MM-dd)
     * @param endDate optional end date (ISO format yyyy-MM-dd)
     * @param authentication the Spring Security authentication
     * @return financial summary with totals and per-category breakdown
     */
    @Operation(summary = "Get financial summary",
            description = "Returns total income, expenses, net balance, and per-category breakdown. Defaults to current month if no dates provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/summary")
    public SummaryResponse getSummary(
            @Parameter(description = "Start date (yyyy-MM-dd), defaults to first day of current month")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd), defaults to last day of current month")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return reportService.getSummary(userId, startDate, endDate);
    }

    /**
     * Returns monthly trend data for the authenticated user over the given date range.
     * If no dates are provided, defaults to the current calendar month.
     *
     * @param startDate optional start date (ISO format yyyy-MM-dd)
     * @param endDate optional end date (ISO format yyyy-MM-dd)
     * @param authentication the Spring Security authentication
     * @return list of monthly trend data points ordered by year and month ascending
     */
    @Operation(summary = "Get monthly trend",
            description = "Returns income, expenses, and net balance grouped by month. Defaults to current month if no dates provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Monthly trend retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/monthly-trend")
    public List<MonthlyTrendResponse> getMonthlyTrend(
            @Parameter(description = "Start date (yyyy-MM-dd), defaults to first day of current month")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd), defaults to last day of current month")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return reportService.getMonthlyTrend(userId, startDate, endDate);
    }

    private Long getUserId(Authentication authentication) {
        return ((com.financetracker.model.User) authentication.getPrincipal()).getId();
    }
}

package com.financetracker.dto.goal;

import com.financetracker.model.GoalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a savings goal including computed progress information.
 *
 * @param id the goal ID
 * @param name the goal name
 * @param targetAmount the target amount to reach
 * @param contributedAmount the total amount contributed so far
 * @param remainingAmount the amount still needed to reach the goal (min 0)
 * @param progressPercentage the percentage of the goal completed, rounded to 2 decimal places
 * @param targetDate the date by which the goal should be achieved
 * @param status the current status (IN_PROGRESS or ACHIEVED)
 * @param createdAt the creation timestamp
 */
@Schema(description = "Savings goal response including computed progress information")
public record GoalResponse(

        @Schema(description = "Unique goal identifier", example = "1")
        Long id,

        @Schema(description = "Goal name", example = "Emergency Fund")
        String name,

        @Schema(description = "Target amount to reach", example = "10000.0000")
        BigDecimal targetAmount,

        @Schema(description = "Total amount contributed so far", example = "2500.0000")
        BigDecimal contributedAmount,

        @Schema(description = "Amount still needed to reach the goal", example = "7500.0000")
        BigDecimal remainingAmount,

        @Schema(description = "Percentage of goal completed (0.00 - 100.00)", example = "25.00")
        BigDecimal progressPercentage,

        @Schema(description = "Target date to achieve the goal", example = "2027-01-01")
        LocalDate targetDate,

        @Schema(description = "Current status of the goal", example = "IN_PROGRESS")
        GoalStatus status,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}

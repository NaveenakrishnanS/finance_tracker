package com.financetracker.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a savings goal.
 *
 * @param name the name of the goal
 * @param targetAmount the target amount to reach
 * @param targetDate the date by which the goal should be achieved
 */
@Schema(description = "Request body for creating or updating a savings goal")
public record GoalRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        @Schema(description = "Name of the savings goal", example = "Emergency Fund")
        String name,

        @NotNull(message = "Target amount is required")
        @DecimalMin(value = "0.01", message = "Target amount must be at least 0.01")
        @Schema(description = "Target amount to reach", example = "10000.00")
        BigDecimal targetAmount,

        @NotNull(message = "Target date is required")
        @Schema(description = "Target date to achieve the goal", example = "2027-01-01")
        LocalDate targetDate
) {}

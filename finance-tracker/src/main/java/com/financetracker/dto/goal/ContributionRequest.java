package com.financetracker.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request DTO for adding a contribution to a savings goal.
 *
 * @param amount the contribution amount
 * @param note optional note about this contribution
 */
@Schema(description = "Request body for adding a contribution to a savings goal")
public record ContributionRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Contribution amount must be at least 0.01")
        @Schema(description = "Contribution amount", example = "250.00")
        BigDecimal amount,

        @Schema(description = "Optional note about this contribution", example = "Monthly savings")
        String note
) {}

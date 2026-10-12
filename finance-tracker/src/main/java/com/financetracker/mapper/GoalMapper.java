package com.financetracker.mapper;

import com.financetracker.dto.goal.GoalResponse;
import com.financetracker.model.Goal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Goal} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface GoalMapper {

    /**
     * Maps a Goal entity to a GoalResponse DTO with computed progress fields.
     *
     * @param goal the Goal entity to map
     * @return the GoalResponse DTO with remainingAmount and progressPercentage computed
     */
    @Mapping(target = "remainingAmount", expression = "java(computeRemainingAmount(goal))")
    @Mapping(target = "progressPercentage", expression = "java(computeProgressPercentage(goal))")
    GoalResponse toResponse(Goal goal);

    /**
     * Computes the remaining amount: max(0, targetAmount - contributedAmount).
     *
     * @param goal the goal entity
     * @return the remaining amount, minimum 0
     */
    default BigDecimal computeRemainingAmount(Goal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getContributedAmount());
        return remaining.max(BigDecimal.ZERO);
    }

    /**
     * Computes the progress percentage: (contributedAmount / targetAmount) * 100,
     * rounded to 2 decimal places with HALF_UP. Returns 0.00 when targetAmount is 0.
     *
     * @param goal the goal entity
     * @return the progress percentage
     */
    default BigDecimal computeProgressPercentage(Goal goal) {
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return goal.getContributedAmount()
                .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

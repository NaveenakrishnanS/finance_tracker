package com.financetracker.mapper;

import com.financetracker.dto.goal.GoalResponse;
import com.financetracker.model.Goal;
import com.financetracker.model.GoalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-13T01:56:04+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class GoalMapperImpl implements GoalMapper {

    @Override
    public GoalResponse toResponse(Goal goal) {
        if ( goal == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        BigDecimal targetAmount = null;
        BigDecimal contributedAmount = null;
        LocalDate targetDate = null;
        GoalStatus status = null;
        LocalDateTime createdAt = null;

        id = goal.getId();
        name = goal.getName();
        targetAmount = goal.getTargetAmount();
        contributedAmount = goal.getContributedAmount();
        targetDate = goal.getTargetDate();
        status = goal.getStatus();
        createdAt = goal.getCreatedAt();

        BigDecimal remainingAmount = computeRemainingAmount(goal);
        BigDecimal progressPercentage = computeProgressPercentage(goal);

        GoalResponse goalResponse = new GoalResponse( id, name, targetAmount, contributedAmount, remainingAmount, progressPercentage, targetDate, status, createdAt );

        return goalResponse;
    }
}

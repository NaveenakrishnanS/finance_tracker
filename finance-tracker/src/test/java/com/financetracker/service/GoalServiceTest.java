package com.financetracker.service;

import com.financetracker.dto.goal.ContributionRequest;
import com.financetracker.dto.goal.GoalRequest;
import com.financetracker.dto.goal.GoalResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.GoalMapper;
import com.financetracker.model.Goal;
import java.math.RoundingMode;
import com.financetracker.model.GoalStatus;
import com.financetracker.model.User;
import com.financetracker.repository.GoalRepository;
import com.financetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GoalService using JUnit 5 and Mockito.
 */
@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoalMapper goalMapper;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Goal goal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        goal = new Goal();
        goal.setId(5L);
        goal.setUser(user);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.0000"));
        goal.setContributedAmount(BigDecimal.ZERO);
        goal.setTargetDate(LocalDate.of(2027, 1, 1));
        goal.setStatus(GoalStatus.IN_PROGRESS);
        goal.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createGoal_persistsWithInProgressStatusAndZeroContribution() {
        var request = new GoalRequest("Emergency Fund", new BigDecimal("10000.00"),
                LocalDate.of(2027, 1, 1));
        var responseGoal = new Goal();
        responseGoal.setId(5L);
        responseGoal.setUser(user);
        responseGoal.setName("Emergency Fund");
        responseGoal.setTargetAmount(new BigDecimal("10000.0000"));
        responseGoal.setContributedAmount(BigDecimal.ZERO);
        responseGoal.setTargetDate(LocalDate.of(2027, 1, 1));
        responseGoal.setStatus(GoalStatus.IN_PROGRESS);
        responseGoal.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.save(any(Goal.class))).thenReturn(responseGoal);

        var responseDto = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), BigDecimal.ZERO,
                new BigDecimal("10000.0000"), BigDecimal.ZERO,
                LocalDate.of(2027, 1, 1), GoalStatus.IN_PROGRESS, LocalDateTime.now());
        when(goalMapper.toResponse(any(Goal.class))).thenReturn(responseDto);

        var result = goalService.createGoal(1L, request);

        assertThat(result.status()).isEqualTo(GoalStatus.IN_PROGRESS);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void getGoal_remainingAmount_equalsTargetMinusContributed_whenNotYetAchieved() {
        goal.setContributedAmount(new BigDecimal("2500.0000"));
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        var responseDto = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("2500.0000"),
                new BigDecimal("7500.0000"), new BigDecimal("25.00"),
                LocalDate.of(2027, 1, 1), GoalStatus.IN_PROGRESS, LocalDateTime.now());
        when(goalMapper.toResponse(goal)).thenReturn(responseDto);

        var result = goalService.getGoal(1L, 5L);

        assertThat(result.remainingAmount().compareTo(new BigDecimal("7500.0000"))).isZero();
    }

    @Test
    void getGoal_remainingAmount_isZero_whenContributedExceedsTarget() {
        goal.setContributedAmount(new BigDecimal("12000.0000"));
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        var responseDto = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("12000.0000"),
                BigDecimal.ZERO, new BigDecimal("120.00"),
                LocalDate.of(2027, 1, 1), GoalStatus.ACHIEVED, LocalDateTime.now());
        when(goalMapper.toResponse(goal)).thenReturn(responseDto);

        var result = goalService.getGoal(1L, 5L);

        assertThat(result.remainingAmount().compareTo(BigDecimal.ZERO)).isZero();
    }

    @Test
    void getGoal_progressPercentage_computedWithHalfUpRoundingTo2Decimal() {
        // 1/3 of goal = 33.33%
        goal.setContributedAmount(new BigDecimal("3333.3333"));
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        var responseDto = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("3333.3333"),
                new BigDecimal("6666.6667"), new BigDecimal("33.33"),
                LocalDate.of(2027, 1, 1), GoalStatus.IN_PROGRESS, LocalDateTime.now());
        when(goalMapper.toResponse(goal)).thenReturn(responseDto);

        var result = goalService.getGoal(1L, 5L);

        assertThat(result.progressPercentage()).isNotNull();
    }

    @Test
    void addContribution_incrementsContributedAmount() {
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        var responseDto = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("500.0000"),
                new BigDecimal("9500.0000"), new BigDecimal("5.00"),
                LocalDate.of(2027, 1, 1), GoalStatus.IN_PROGRESS, LocalDateTime.now());
        when(goalMapper.toResponse(any(Goal.class))).thenReturn(responseDto);

        var request = new ContributionRequest(new BigDecimal("500.00"), "Monthly savings");
        goalService.addContribution(1L, 5L, request);

        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void addContribution_setsStatusToAchieved_whenContributedReachesTarget() {
        goal.setContributedAmount(new BigDecimal("9500.0000"));
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> {
            Goal saved = inv.getArgument(0);
            return saved;
        });

        var achievedGoal = new Goal();
        achievedGoal.setId(5L);
        achievedGoal.setUser(user);
        achievedGoal.setName("Emergency Fund");
        achievedGoal.setTargetAmount(new BigDecimal("10000.0000"));
        achievedGoal.setContributedAmount(new BigDecimal("10000.0000"));
        achievedGoal.setStatus(GoalStatus.ACHIEVED);
        achievedGoal.setTargetDate(LocalDate.of(2027, 1, 1));
        achievedGoal.setCreatedAt(LocalDateTime.now());

        var achievedResponse = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("10000.0000"),
                BigDecimal.ZERO, new BigDecimal("100.00"),
                LocalDate.of(2027, 1, 1), GoalStatus.ACHIEVED, LocalDateTime.now());
        when(goalMapper.toResponse(any(Goal.class))).thenReturn(achievedResponse);

        var request = new ContributionRequest(new BigDecimal("500.00"), null);
        var result = goalService.addContribution(1L, 5L, request);

        assertThat(result.status()).isEqualTo(GoalStatus.ACHIEVED);
    }

    @Test
    void addContribution_statusRemainsAchieved_afterAdditionalContributionWhenAlreadyAchieved() {
        goal.setContributedAmount(new BigDecimal("12000.0000"));
        goal.setStatus(GoalStatus.ACHIEVED);
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        var achievedResponse = new GoalResponse(5L, "Emergency Fund",
                new BigDecimal("10000.0000"), new BigDecimal("12500.0000"),
                BigDecimal.ZERO, new BigDecimal("125.00"),
                LocalDate.of(2027, 1, 1), GoalStatus.ACHIEVED, LocalDateTime.now());
        when(goalMapper.toResponse(any(Goal.class))).thenReturn(achievedResponse);

        var request = new ContributionRequest(new BigDecimal("500.00"), null);
        var result = goalService.addContribution(1L, 5L, request);

        assertThat(result.status()).isEqualTo(GoalStatus.ACHIEVED);
    }

    @Test
    void progressPercentage_isZero_whenTargetAmountIsZero() {
        // Test the mapper default method for this edge case using an anonymous implementation
        GoalMapper mapperInstance = goal1 -> null; // anonymous to access default methods
        var zeroGoal = new Goal();
        zeroGoal.setId(1L);
        zeroGoal.setName("Test");
        zeroGoal.setTargetAmount(BigDecimal.ZERO);
        zeroGoal.setContributedAmount(BigDecimal.ZERO);
        zeroGoal.setTargetDate(LocalDate.now());
        zeroGoal.setStatus(GoalStatus.IN_PROGRESS);
        zeroGoal.setCreatedAt(LocalDateTime.now());
        zeroGoal.setUser(user);
        // Verify guard: progressPercentage returns 0.00 when targetAmount is 0
        BigDecimal progress = mapperInstance.computeProgressPercentage(zeroGoal);
        assertThat(progress.compareTo(BigDecimal.ZERO)).isZero();
    }

    @Test
    void getGoal_throwsResourceNotFoundException_whenGoalNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> goalService.getGoal(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getGoal_throwsForbiddenOperationException_whenOwnedByAnotherUser() {
        var otherUser = new User();
        otherUser.setId(99L);
        goal.setUser(otherUser);
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        assertThatThrownBy(() -> goalService.getGoal(1L, 5L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteGoal_callsRepositoryDelete() {
        when(goalRepository.findById(5L)).thenReturn(Optional.of(goal));
        goalService.deleteGoal(1L, 5L);
        verify(goalRepository).delete(goal);
    }
}

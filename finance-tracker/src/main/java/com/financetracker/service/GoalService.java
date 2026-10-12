package com.financetracker.service;

import com.financetracker.dto.goal.ContributionRequest;
import com.financetracker.dto.goal.GoalRequest;
import com.financetracker.dto.goal.GoalResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.GoalMapper;
import com.financetracker.model.Goal;
import com.financetracker.model.GoalStatus;
import com.financetracker.repository.GoalRepository;
import com.financetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing savings goals.
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalMapper goalMapper;

    /**
     * Constructs GoalService with required dependencies.
     *
     * @param goalRepository the goal data repository
     * @param userRepository the user data repository
     * @param goalMapper the goal entity to DTO mapper
     */
    public GoalService(GoalRepository goalRepository,
                        UserRepository userRepository,
                        GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.goalMapper = goalMapper;
    }

    /**
     * Creates a new savings goal for the given user.
     *
     * @param userId the ID of the user creating the goal
     * @param request the goal creation request
     * @return the created goal response with computed progress fields
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var goal = new Goal();
        goal.setUser(user);
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());
        goal.setContributedAmount(BigDecimal.ZERO);
        goal.setStatus(GoalStatus.IN_PROGRESS);
        goal.setCreatedAt(LocalDateTime.now());
        var saved = goalRepository.save(goal);
        return goalMapper.toResponse(saved);
    }

    /**
     * Returns all goals for a user with computed progress fields.
     *
     * @param userId the user ID
     * @return list of goal responses
     */
    public List<GoalResponse> getGoals(Long userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    /**
     * Returns a single goal by ID with computed progress fields.
     *
     * @param userId the ID of the requesting user
     * @param id the goal ID
     * @return the goal response
     * @throws ResourceNotFoundException if the goal does not exist
     * @throws ForbiddenOperationException if the goal belongs to another user
     */
    public GoalResponse getGoal(Long userId, Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to access this goal.");
        }
        return goalMapper.toResponse(goal);
    }

    /**
     * Updates the name, target amount, and target date of an existing goal.
     *
     * @param userId the ID of the requesting user
     * @param id the goal ID to update
     * @param request the update request
     * @return the updated goal response
     * @throws ResourceNotFoundException if the goal does not exist
     * @throws ForbiddenOperationException if the goal belongs to another user
     */
    @Transactional
    public GoalResponse updateGoal(Long userId, Long id, GoalRequest request) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to update this goal.");
        }
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());
        var saved = goalRepository.save(goal);
        return goalMapper.toResponse(saved);
    }

    /**
     * Deletes a goal by ID, enforcing ownership.
     *
     * @param userId the ID of the requesting user
     * @param id the goal ID to delete
     * @throws ResourceNotFoundException if the goal does not exist
     * @throws ForbiddenOperationException if the goal belongs to another user
     */
    @Transactional
    public void deleteGoal(Long userId, Long id) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to delete this goal.");
        }
        goalRepository.delete(goal);
    }

    /**
     * Adds a contribution to a goal, incrementing the contributed amount.
     * If the contributed amount reaches or exceeds the target, the status is set to ACHIEVED.
     *
     * @param userId the ID of the requesting user
     * @param id the goal ID
     * @param request the contribution request containing the amount
     * @return the updated goal response
     * @throws ResourceNotFoundException if the goal does not exist
     * @throws ForbiddenOperationException if the goal belongs to another user
     */
    @Transactional
    public GoalResponse addContribution(Long userId, Long id, ContributionRequest request) {
        var goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
        if (!goal.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to contribute to this goal.");
        }
        var newContributed = goal.getContributedAmount().add(request.amount());
        goal.setContributedAmount(newContributed);
        if (newContributed.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.ACHIEVED);
        }
        var saved = goalRepository.save(goal);
        return goalMapper.toResponse(saved);
    }
}

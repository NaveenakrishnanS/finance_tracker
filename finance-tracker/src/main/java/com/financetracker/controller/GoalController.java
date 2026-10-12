package com.financetracker.controller;

import com.financetracker.dto.goal.ContributionRequest;
import com.financetracker.dto.goal.GoalRequest;
import com.financetracker.dto.goal.GoalResponse;
import com.financetracker.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing savings goals.
 */
@RestController
@RequestMapping("/api/v1/goals")
@Tag(name = "Goals", description = "Savings goal management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    /**
     * Constructs GoalController with required dependencies.
     *
     * @param goalService the goal service
     */
    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Returns all goals for the authenticated user.
     *
     * @param authentication the Spring Security authentication
     * @return list of goal responses with progress fields
     */
    @Operation(summary = "Get all goals", description = "Returns all savings goals for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goals retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public List<GoalResponse> getGoals(Authentication authentication) {
        Long userId = getUserId(authentication);
        return goalService.getGoals(userId);
    }

    /**
     * Creates a new savings goal for the authenticated user.
     *
     * @param request the goal creation request
     * @param authentication the Spring Security authentication
     * @return the created goal response
     */
    @Operation(summary = "Create a goal", description = "Creates a new savings goal")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Goal created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse createGoal(@Valid @RequestBody GoalRequest request,
                                    Authentication authentication) {
        Long userId = getUserId(authentication);
        return goalService.createGoal(userId, request);
    }

    /**
     * Returns a single goal by ID.
     *
     * @param id the goal ID
     * @param authentication the Spring Security authentication
     * @return the goal response
     */
    @Operation(summary = "Get a goal by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @GetMapping("/{id}")
    public GoalResponse getGoal(
            @Parameter(description = "Goal ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return goalService.getGoal(userId, id);
    }

    /**
     * Updates an existing goal.
     *
     * @param id the goal ID
     * @param request the update request
     * @param authentication the Spring Security authentication
     * @return the updated goal response
     */
    @Operation(summary = "Update a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Goal updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PutMapping("/{id}")
    public GoalResponse updateGoal(
            @Parameter(description = "Goal ID") @PathVariable Long id,
            @Valid @RequestBody GoalRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return goalService.updateGoal(userId, id, request);
    }

    /**
     * Deletes a goal by ID.
     *
     * @param id the goal ID
     * @param authentication the Spring Security authentication
     */
    @Operation(summary = "Delete a goal")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoal(
            @Parameter(description = "Goal ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        goalService.deleteGoal(userId, id);
    }

    /**
     * Adds a contribution to a savings goal.
     *
     * @param id the goal ID
     * @param request the contribution request
     * @param authentication the Spring Security authentication
     * @return the updated goal response with new progress fields
     */
    @Operation(summary = "Add a contribution to a goal",
            description = "Adds an amount to contributedAmount; sets status to ACHIEVED when target is met")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contribution added successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PostMapping("/{id}/contributions")
    public GoalResponse addContribution(
            @Parameter(description = "Goal ID") @PathVariable Long id,
            @Valid @RequestBody ContributionRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return goalService.addContribution(userId, id, request);
    }

    private Long getUserId(Authentication authentication) {
        return ((com.financetracker.model.User) authentication.getPrincipal()).getId();
    }
}

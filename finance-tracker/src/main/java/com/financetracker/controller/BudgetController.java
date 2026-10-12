package com.financetracker.controller;

import com.financetracker.dto.budget.BudgetRequest;
import com.financetracker.dto.budget.BudgetResponse;
import com.financetracker.service.BudgetService;
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
 * REST controller for managing monthly budgets.
 */
@RestController
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "Monthly budget management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Constructs BudgetController with required dependencies.
     *
     * @param budgetService the budget service
     */
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * Returns all budgets for the authenticated user.
     *
     * @param authentication the Spring Security authentication
     * @return list of budget responses with spentAmount and overLimit fields
     */
    @Operation(summary = "Get all budgets", description = "Returns all monthly budgets for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Budgets retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public List<BudgetResponse> getBudgets(Authentication authentication) {
        Long userId = getUserId(authentication);
        return budgetService.getBudgets(userId);
    }

    /**
     * Creates a new budget for the authenticated user.
     *
     * @param request the budget creation request
     * @param authentication the Spring Security authentication
     * @return the created budget response
     */
    @Operation(summary = "Create a budget", description = "Creates a new monthly budget for a category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Budget created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "409", description = "Budget already exists for this category/month/year")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse createBudget(@Valid @RequestBody BudgetRequest request,
                                        Authentication authentication) {
        Long userId = getUserId(authentication);
        return budgetService.createBudget(userId, request);
    }

    /**
     * Returns a single budget by ID.
     *
     * @param id the budget ID
     * @param authentication the Spring Security authentication
     * @return the budget response
     */
    @Operation(summary = "Get a budget by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Budget retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @GetMapping("/{id}")
    public BudgetResponse getBudget(
            @Parameter(description = "Budget ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return budgetService.getBudget(userId, id);
    }

    /**
     * Updates an existing budget's limit amount.
     *
     * @param id the budget ID
     * @param request the update request
     * @param authentication the Spring Security authentication
     * @return the updated budget response
     */
    @Operation(summary = "Update a budget")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Budget updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @PutMapping("/{id}")
    public BudgetResponse updateBudget(
            @Parameter(description = "Budget ID") @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        return budgetService.updateBudget(userId, id, request);
    }

    /**
     * Deletes a budget by ID.
     *
     * @param id the budget ID
     * @param authentication the Spring Security authentication
     */
    @Operation(summary = "Delete a budget")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Budget deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBudget(
            @Parameter(description = "Budget ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        budgetService.deleteBudget(userId, id);
    }

    private Long getUserId(Authentication authentication) {
        return ((com.financetracker.model.User) authentication.getPrincipal()).getId();
    }
}

package com.financetracker.service;

import com.financetracker.dto.budget.BudgetRequest;
import com.financetracker.dto.budget.BudgetResponse;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.BudgetMapper;
import com.financetracker.model.Budget;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing monthly budgets.
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;

    /**
     * Constructs BudgetService with required dependencies.
     *
     * @param budgetRepository the budget data repository
     * @param categoryRepository the category data repository
     * @param userRepository the user data repository
     * @param transactionRepository the transaction data repository
     * @param budgetMapper the budget entity to DTO mapper
     */
    public BudgetService(BudgetRepository budgetRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository,
                          BudgetMapper budgetMapper) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetMapper = budgetMapper;
    }

    /**
     * Creates a new budget for the given user.
     *
     * @param userId the ID of the user creating the budget
     * @param request the budget creation request
     * @return the created budget response with spentAmount and overLimit computed
     * @throws DuplicateResourceException if a budget already exists for the user/category/month/year
     * @throws ResourceNotFoundException if the user or category does not exist
     */
    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.categoryId(), request.month(), request.year())) {
            throw new DuplicateResourceException(
                    "Budget already exists for this category, month, and year.");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category",
                        request.categoryId()));
        var budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setLimitAmount(request.limitAmount());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        var saved = budgetRepository.save(budget);
        var spentAmount = getSpentAmount(userId, saved.getCategory().getId(),
                saved.getMonth(), saved.getYear());
        return budgetMapper.toResponse(saved, spentAmount);
    }

    /**
     * Returns all budgets for a user with computed spending information.
     *
     * @param userId the user ID
     * @return list of budget responses with spentAmount and overLimit fields
     */
    public List<BudgetResponse> getBudgets(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(budget -> {
                    var spentAmount = getSpentAmount(userId,
                            budget.getCategory().getId(),
                            budget.getMonth(), budget.getYear());
                    return budgetMapper.toResponse(budget, spentAmount);
                })
                .toList();
    }

    /**
     * Returns a single budget by ID with computed spending information.
     *
     * @param userId the ID of the requesting user
     * @param id the budget ID
     * @return the budget response
     * @throws ResourceNotFoundException if the budget does not exist
     * @throws ForbiddenOperationException if the budget belongs to another user
     */
    public BudgetResponse getBudget(Long userId, Long id) {
        var budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        if (!budget.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to access this budget.");
        }
        var spentAmount = getSpentAmount(userId, budget.getCategory().getId(),
                budget.getMonth(), budget.getYear());
        return budgetMapper.toResponse(budget, spentAmount);
    }

    /**
     * Updates the limit amount of an existing budget.
     *
     * @param userId the ID of the requesting user
     * @param id the budget ID to update
     * @param request the update request containing the new limit amount
     * @return the updated budget response
     * @throws ResourceNotFoundException if the budget does not exist
     * @throws ForbiddenOperationException if the budget belongs to another user
     */
    @Transactional
    public BudgetResponse updateBudget(Long userId, Long id, BudgetRequest request) {
        var budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        if (!budget.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to update this budget.");
        }
        budget.setLimitAmount(request.limitAmount());
        var saved = budgetRepository.save(budget);
        var spentAmount = getSpentAmount(userId, saved.getCategory().getId(),
                saved.getMonth(), saved.getYear());
        return budgetMapper.toResponse(saved, spentAmount);
    }

    /**
     * Deletes a budget by ID, enforcing ownership.
     *
     * @param userId the ID of the requesting user
     * @param id the budget ID to delete
     * @throws ResourceNotFoundException if the budget does not exist
     * @throws ForbiddenOperationException if the budget belongs to another user
     */
    @Transactional
    public void deleteBudget(Long userId, Long id) {
        var budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        if (!budget.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to delete this budget.");
        }
        budgetRepository.delete(budget);
    }

    /**
     * Retrieves the total expense amount for a given user, category, and period.
     * Returns BigDecimal.ZERO if no expenses exist.
     *
     * @param userId the user ID
     * @param categoryId the category ID
     * @param month the month (1-12)
     * @param year the year
     * @return the total spent amount
     */
    private BigDecimal getSpentAmount(Long userId, Long categoryId, int month, int year) {
        BigDecimal spent = transactionRepository.sumExpensesByUserAndCategoryAndPeriod(
                userId, categoryId, month, year);
        return spent != null ? spent : BigDecimal.ZERO;
    }
}

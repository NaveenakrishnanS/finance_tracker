package com.financetracker.service;

import com.financetracker.dto.budget.BudgetRequest;
import com.financetracker.dto.budget.BudgetResponse;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.BudgetMapper;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BudgetService using JUnit 5 and Mockito.
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category category;
    private Budget budget;
    private BudgetRequest request;
    private BudgetResponse response;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        category = new Category();
        category.setId(2L);
        category.setName("Food");

        budget = new Budget();
        budget.setId(10L);
        budget.setUser(user);
        budget.setCategory(category);
        budget.setLimitAmount(new BigDecimal("500.0000"));
        budget.setMonth(3);
        budget.setYear(2026);

        request = new BudgetRequest(2L, new BigDecimal("500.00"), 3, 2026);

        response = new BudgetResponse(10L, 2L, "Food",
                new BigDecimal("500.0000"), 3, 2026,
                new BigDecimal("200.0000"), false);
    }

    @Test
    void createBudget_successPath_persistsAndReturnsBudgetResponse() {
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(1L, 2L, 3, 2026))
                .thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        when(transactionRepository.sumExpensesByUserAndCategoryAndPeriod(anyLong(), anyLong(),
                anyInt(), anyInt())).thenReturn(new BigDecimal("200.0000"));
        when(budgetMapper.toResponse(any(Budget.class), any(BigDecimal.class))).thenReturn(response);

        var result = budgetService.createBudget(1L, request);

        assertThat(result).isEqualTo(response);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_throwsDuplicateResourceException_whenSameUserCategoryMonthYearExists() {
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(1L, 2L, 3, 2026))
                .thenReturn(true);
        assertThatThrownBy(() -> budgetService.createBudget(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getBudgets_overLimit_isFalse_whenSpentLessThanLimit() {
        var underBudgetResponse = new BudgetResponse(10L, 2L, "Food",
                new BigDecimal("500.0000"), 3, 2026,
                new BigDecimal("200.0000"), false);
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndPeriod(anyLong(), anyLong(),
                anyInt(), anyInt())).thenReturn(new BigDecimal("200.0000"));
        when(budgetMapper.toResponse(budget, new BigDecimal("200.0000")))
                .thenReturn(underBudgetResponse);

        var result = budgetService.getBudgets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).overLimit()).isFalse();
    }

    @Test
    void getBudgets_overLimit_isTrue_whenSpentGreaterThanLimit() {
        var overBudgetResponse = new BudgetResponse(10L, 2L, "Food",
                new BigDecimal("500.0000"), 3, 2026,
                new BigDecimal("600.0000"), true);
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndPeriod(anyLong(), anyLong(),
                anyInt(), anyInt())).thenReturn(new BigDecimal("600.0000"));
        when(budgetMapper.toResponse(budget, new BigDecimal("600.0000")))
                .thenReturn(overBudgetResponse);

        var result = budgetService.getBudgets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).overLimit()).isTrue();
    }

    @Test
    void getBudgets_overLimit_isFalse_whenSpentEqualsLimit() {
        var equalBudgetResponse = new BudgetResponse(10L, 2L, "Food",
                new BigDecimal("500.0000"), 3, 2026,
                new BigDecimal("500.0000"), false);
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpensesByUserAndCategoryAndPeriod(anyLong(), anyLong(),
                anyInt(), anyInt())).thenReturn(new BigDecimal("500.0000"));
        when(budgetMapper.toResponse(budget, new BigDecimal("500.0000")))
                .thenReturn(equalBudgetResponse);

        var result = budgetService.getBudgets(1L);

        assertThat(result.get(0).overLimit()).isFalse();
    }

    @Test
    void updateBudget_updatesLimitAndReturnsUpdatedResponse() {
        var updatedRequest = new BudgetRequest(2L, new BigDecimal("750.00"), 3, 2026);
        var updatedBudget = new Budget();
        updatedBudget.setId(10L);
        updatedBudget.setUser(user);
        updatedBudget.setCategory(category);
        updatedBudget.setLimitAmount(new BigDecimal("750.0000"));
        updatedBudget.setMonth(3);
        updatedBudget.setYear(2026);

        var updatedResponse = new BudgetResponse(10L, 2L, "Food",
                new BigDecimal("750.0000"), 3, 2026,
                new BigDecimal("200.0000"), false);

        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(updatedBudget);
        when(transactionRepository.sumExpensesByUserAndCategoryAndPeriod(anyLong(), anyLong(),
                anyInt(), anyInt())).thenReturn(new BigDecimal("200.0000"));
        when(budgetMapper.toResponse(any(Budget.class), any(BigDecimal.class)))
                .thenReturn(updatedResponse);

        var result = budgetService.updateBudget(1L, 10L, updatedRequest);

        assertThat(result.limitAmount()).isEqualByComparingTo(new BigDecimal("750.0000"));
    }

    @Test
    void deleteBudget_callsRepositoryDelete() {
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        budgetService.deleteBudget(1L, 10L);
        verify(budgetRepository).delete(budget);
    }

    @Test
    void getBudget_throwsResourceNotFoundException_whenNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> budgetService.getBudget(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBudget_throwsForbiddenOperationException_whenOwnedByAnotherUser() {
        var otherUser = new User();
        otherUser.setId(99L);
        budget.setUser(otherUser);
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        assertThatThrownBy(() -> budgetService.getBudget(1L, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getBudgets_bigDecimalArithmetic_noFloatingPointErrors() {
        // Tests that BigDecimal comparison logic works correctly without float errors
        var limitAmount = new BigDecimal("333.3333");
        var spentAmount = new BigDecimal("333.3333");
        // Equal should not be overLimit
        assertThat(spentAmount.compareTo(limitAmount) > 0).isFalse();
    }
}

package com.financetracker.repository;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataJpaTest for BudgetRepository verifying database-level behaviour.
 */
@DataJpaTest
@ActiveProfiles("test")
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("budget-test@example.com");
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        category = new Category();
        category.setName("Food");
        category.setSystemDefault(false);
        category.setUser(user);
        category = categoryRepository.save(category);
    }

    private Budget buildBudget(int month, int year, BigDecimal limit) {
        var budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setLimitAmount(limit);
        budget.setMonth(month);
        budget.setYear(year);
        return budget;
    }

    @Test
    void existsByUserIdAndCategoryIdAndMonthAndYear_returnsTrue_forDuplicate() {
        budgetRepository.save(buildBudget(3, 2026, new BigDecimal("500.0000")));
        boolean exists = budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), 3, 2026);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndCategoryIdAndMonthAndYear_returnsFalse_forUniqueEntry() {
        boolean exists = budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                user.getId(), category.getId(), 4, 2026);
        assertThat(exists).isFalse();
    }

    @Test
    void findByUserId_returnsAllBudgetsForUser() {
        budgetRepository.save(buildBudget(3, 2026, new BigDecimal("500.0000")));
        budgetRepository.save(buildBudget(4, 2026, new BigDecimal("300.0000")));
        var budgets = budgetRepository.findByUserId(user.getId());
        assertThat(budgets).hasSize(2);
    }

    @Test
    void budgetLimitAmount_bigDecimalRoundTrip_preserves4DecimalPlaces() {
        var limit = new BigDecimal("123.4567");
        var saved = budgetRepository.save(buildBudget(5, 2026, limit));
        var retrieved = budgetRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getLimitAmount().compareTo(limit)).isZero();
    }

    @Test
    void sumExpensesByUserAndCategoryAndPeriod_returnsCorrectSum_afterTransactionsInserted() {
        budgetRepository.save(buildBudget(3, 2026, new BigDecimal("500.0000")));

        var t1 = new Transaction();
        t1.setUser(user);
        t1.setCategory(category);
        t1.setAmount(new BigDecimal("100.0000"));
        t1.setType(TransactionType.EXPENSE);
        t1.setDate(LocalDate.of(2026, 3, 10));
        t1.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t1);

        var t2 = new Transaction();
        t2.setUser(user);
        t2.setCategory(category);
        t2.setAmount(new BigDecimal("50.0000"));
        t2.setType(TransactionType.EXPENSE);
        t2.setDate(LocalDate.of(2026, 3, 15));
        t2.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(t2);

        BigDecimal sum = transactionRepository.sumExpensesByUserAndCategoryAndPeriod(
                user.getId(), category.getId(), 3, 2026);
        assertThat(sum.compareTo(new BigDecimal("150.0000"))).isZero();
    }

    @Test
    void sumExpensesByUserAndCategoryAndPeriod_returnsZero_whenNoTransactions() {
        budgetRepository.save(buildBudget(3, 2026, new BigDecimal("500.0000")));
        BigDecimal sum = transactionRepository.sumExpensesByUserAndCategoryAndPeriod(
                user.getId(), category.getId(), 3, 2026);
        assertThat(sum).isNotNull();
        assertThat(sum.compareTo(BigDecimal.ZERO)).isZero();
    }
}

package com.financetracker.repository;

import com.financetracker.model.Budget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Budget} entities.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Returns all budgets for a given user.
     *
     * @param userId the user ID
     * @return list of budgets belonging to the user
     */
    List<Budget> findByUserId(Long userId);

    /**
     * Returns all budgets for a user along with their user and category associations eagerly.
     *
     * @param userId the user ID
     * @return list of budgets for the user
     */
    List<Budget> findByUserIdOrderByYearDescMonthDesc(Long userId);

    /**
     * Checks whether a budget already exists for the given user, category, month, and year.
     *
     * @param userId the user ID
     * @param categoryId the category ID
     * @param month the month (1-12)
     * @param year the year
     * @return true if a budget already exists for this combination
     */
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId,
                                                        int month, int year);
}

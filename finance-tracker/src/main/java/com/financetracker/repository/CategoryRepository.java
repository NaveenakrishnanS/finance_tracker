package com.financetracker.repository;

import com.financetracker.model.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Category} entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Returns all system-default categories plus any categories owned by the given user.
     *
     * @param userId the user's ID
     * @return list of available categories
     */
    List<Category> findByUserIsNullOrUserId(Long userId);

    /**
     * Checks whether a category with the given name already exists for the given user.
     *
     * @param name the category name to check
     * @param userId the user's ID
     * @return true if a duplicate exists
     */
    boolean existsByNameAndUserId(String name, Long userId);

    /**
     * Checks whether the given category is a system-default (has no user owner).
     *
     * @param id the category ID to check
     * @return true if the category is a system default
     */
    boolean existsByIdAndUserIsNull(Long id);

    /**
     * Counts the number of transactions and budgets that reference the given category.
     * Used to enforce referential integrity before deletion.
     *
     * @param categoryId the category ID to check
     * @return the total number of references to this category
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category.id = :categoryId")
    long countTransactionsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Counts the number of budgets that reference the given category.
     *
     * @param categoryId the category ID to check
     * @return the number of budgets referencing this category
     */
    @Query("SELECT COUNT(b) FROM Budget b WHERE b.category.id = :categoryId")
    long countBudgetsByCategoryId(@Param("categoryId") Long categoryId);
}

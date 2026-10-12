package com.financetracker.repository;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Transaction} entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    /**
     * Returns all transactions for a user, ordered by date descending.
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return paginated list of transactions
     */
    Page<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    /**
     * Returns filtered transactions for a user supporting optional filters.
     *
     * @param userId the user ID
     * @param type optional transaction type filter
     * @param categoryId optional category ID filter
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param pageable pagination parameters
     * @return paginated filtered list of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "ORDER BY t.date DESC")
    Page<Transaction> findWithFilters(@Param("userId") Long userId,
                                      @Param("type") TransactionType type,
                                      @Param("categoryId") Long categoryId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);

    /**
     * Aggregation query for report summary: groups by category and type within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of object arrays [categoryId, categoryName, type, sum]
     */
    @Query("SELECT t.category.id, t.category.name, t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "GROUP BY t.category.id, t.category.name, t.type")
    List<Object[]> findSummaryByCategory(@Param("userId") Long userId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Aggregation query for monthly trend: groups by year and month.
     *
     * @param userId the user ID
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of object arrays [year, month, type, sum]
     */
    @Query("SELECT FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type, SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.date >= :startDate AND t.date <= :endDate " +
           "GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date), t.type " +
           "ORDER BY FUNCTION('YEAR', t.date) ASC, FUNCTION('MONTH', t.date) ASC")
    List<Object[]> findMonthlyTrend(@Param("userId") Long userId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Sums all expense transactions for a user in a given category and period.
     *
     * @param userId the user ID
     * @param categoryId the category ID
     * @param month the month (1-12)
     * @param year the year
     * @return the total spent amount or null if no transactions
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId " +
           "AND t.category.id = :categoryId " +
           "AND t.type = 'EXPENSE' " +
           "AND FUNCTION('MONTH', t.date) = :month " +
           "AND FUNCTION('YEAR', t.date) = :year")
    BigDecimal sumExpensesByUserAndCategoryAndPeriod(@Param("userId") Long userId,
                                                      @Param("categoryId") Long categoryId,
                                                      @Param("month") int month,
                                                      @Param("year") int year);
}

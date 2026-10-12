package com.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/**
 * JPA entity representing a monthly budget limit for a category.
 * Uses optimistic locking via the @Version field to handle concurrent updates.
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_id", "month", "year"})
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int year;

    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Default constructor required by JPA.
     */
    public Budget() {}

    /**
     * Returns the budget's unique identifier.
     *
     * @return the budget id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the budget's unique identifier.
     *
     * @param id the budget id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user who owns this budget.
     *
     * @return the owning user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who owns this budget.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the category this budget applies to.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the category this budget applies to.
     *
     * @param category the category
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Returns the spending limit for this budget period.
     *
     * @return the limit amount as BigDecimal with precision 19, scale 4
     */
    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    /**
     * Sets the spending limit for this budget period.
     *
     * @param limitAmount the limit amount
     */
    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    /**
     * Returns the month (1-12) this budget applies to.
     *
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month this budget applies to.
     *
     * @param month the month (1-12)
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * Returns the year this budget applies to.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year this budget applies to.
     *
     * @param year the year
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns the optimistic locking version.
     *
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the optimistic locking version.
     *
     * @param version the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}

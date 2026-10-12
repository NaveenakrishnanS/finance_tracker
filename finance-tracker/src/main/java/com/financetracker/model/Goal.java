package com.financetracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a savings goal.
 */
@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(name = "contributed_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal contributedAmount = BigDecimal.ZERO;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalStatus status = GoalStatus.IN_PROGRESS;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public Goal() {}

    /**
     * Returns the goal's unique identifier.
     *
     * @return the goal id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the goal's unique identifier.
     *
     * @param id the goal id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user who owns this goal.
     *
     * @return the owning user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who owns this goal.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the goal name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the goal name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the target amount for this goal.
     *
     * @return the target amount as BigDecimal with precision 19, scale 4
     */
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    /**
     * Sets the target amount for this goal.
     *
     * @param targetAmount the target amount
     */
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    /**
     * Returns the total amount contributed so far.
     *
     * @return the contributed amount as BigDecimal with precision 19, scale 4
     */
    public BigDecimal getContributedAmount() {
        return contributedAmount;
    }

    /**
     * Sets the total amount contributed so far.
     *
     * @param contributedAmount the contributed amount
     */
    public void setContributedAmount(BigDecimal contributedAmount) {
        this.contributedAmount = contributedAmount;
    }

    /**
     * Returns the target date by which this goal should be achieved.
     *
     * @return the target date
     */
    public LocalDate getTargetDate() {
        return targetDate;
    }

    /**
     * Sets the target date.
     *
     * @param targetDate the target date
     */
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    /**
     * Returns the current status of this goal.
     *
     * @return the goal status
     */
    public GoalStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of this goal.
     *
     * @param status the goal status
     */
    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the created-at timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the created-at timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

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

/**
 * JPA entity representing a transaction category.
 * Categories can be system defaults (available to all users) or user-created.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_system_default", nullable = false)
    private boolean isSystemDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Default constructor required by JPA.
     */
    public Category() {}

    /**
     * Returns the category's unique identifier.
     *
     * @return the category id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the category's unique identifier.
     *
     * @param id the category id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the category name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether this is a system-default category.
     *
     * @return true if system default, false if user-created
     */
    public boolean isSystemDefault() {
        return isSystemDefault;
    }

    /**
     * Sets whether this is a system-default category.
     *
     * @param isSystemDefault true if system default
     */
    public void setSystemDefault(boolean isSystemDefault) {
        this.isSystemDefault = isSystemDefault;
    }

    /**
     * Returns the user who owns this category, or null for system defaults.
     *
     * @return the owning user, or null
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the owner of this category.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }
}

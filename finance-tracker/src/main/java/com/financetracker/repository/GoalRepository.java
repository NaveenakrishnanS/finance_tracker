package com.financetracker.repository;

import com.financetracker.model.Goal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Goal} entities.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Returns all goals for a given user.
     *
     * @param userId the user ID
     * @return list of goals belonging to the user
     */
    List<Goal> findByUserId(Long userId);
}

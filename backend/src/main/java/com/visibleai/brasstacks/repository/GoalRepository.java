package com.visibleai.brasstacks.repository;

import com.visibleai.brasstacks.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Goal.Status status);
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);
}

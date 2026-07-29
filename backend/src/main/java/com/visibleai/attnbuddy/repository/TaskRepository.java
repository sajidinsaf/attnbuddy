package com.visibleai.attnbuddy.repository;

import com.visibleai.attnbuddy.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.domain
        WHERE t.user.id = :userId
          AND t.status = 'PENDING'
          AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= :now)
        """)
    List<Task> findPendingTasksForUser(@Param("userId") Long userId, @Param("now") Instant now);

    Page<Task> findByUserIdAndStatus(Long userId, Task.Status status, Pageable pageable);

    Page<Task> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, Task.Status status);
}

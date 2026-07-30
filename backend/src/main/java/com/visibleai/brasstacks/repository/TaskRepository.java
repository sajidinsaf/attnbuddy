package com.visibleai.brasstacks.repository;

import com.visibleai.brasstacks.model.Task;
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
          AND t.parentTask IS NULL
          AND (
            (t.status = 'PENDING' AND (t.snoozedUntil IS NULL OR t.snoozedUntil <= :now))
            OR (t.status = 'SNOOZED' AND t.snoozedUntil <= :now)
          )
        """)
    List<Task> findPendingTasksForUser(@Param("userId") Long userId, @Param("now") Instant now);

    Page<Task> findByUserIdAndStatus(Long userId, Task.Status status, Pageable pageable);

    Page<Task> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, Task.Status status);

    @Query("""
        SELECT t FROM Task t
        WHERE t.parentTask.id = :parentId
        ORDER BY t.position ASC, t.id ASC
        """)
    List<Task> findMicroStepsByParentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.parentTask.id = :parentId AND t.status <> 'DONE'")
    long countIncompleteMicroSteps(@Param("parentId") Long parentId);

    @Query("SELECT COALESCE(MAX(t.position), -1) FROM Task t WHERE t.parentTask.id = :parentId")
    int findMaxPositionByParentId(@Param("parentId") Long parentId);
}

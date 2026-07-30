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

    Page<Task> findByUserIdAndStatusAndParentTaskIsNull(Long userId, Task.Status status, Pageable pageable);

    Page<Task> findByUserIdAndParentTaskIsNull(Long userId, Pageable pageable);

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

    // Energy pattern query: find the user's most productive hours
    @Query("""
        SELECT t.completedHour, COUNT(t) FROM Task t
        WHERE t.user.id = :userId
          AND t.status = 'DONE'
          AND t.completedHour IS NOT NULL
          AND t.completedAt > :since
        GROUP BY t.completedHour
        ORDER BY COUNT(t) DESC
        """)
    List<Object[]> findCompletionPatternByHour(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = 'DONE' AND t.completedAt > :since")
    long countCompletedSince(@Param("userId") Long userId, @Param("since") Instant since);

    // Nudge queries
    @Query("""
        SELECT t FROM Task t
        WHERE t.user.id = :userId
          AND t.status = 'PENDING'
          AND t.parentTask IS NULL
          AND t.dueDate IS NOT NULL
          AND t.dueDate BETWEEN :now AND :horizon
        ORDER BY t.dueDate ASC
        """)
    List<Task> findTasksWithDeadlineBetween(@Param("userId") Long userId,
                                             @Param("now") Instant now,
                                             @Param("horizon") Instant horizon);

    @Query("""
        SELECT t FROM Task t
        WHERE t.user.id = :userId
          AND t.status = 'PENDING'
          AND t.parentTask IS NULL
          AND t.createdAt < :staleThreshold
        ORDER BY t.createdAt ASC
        """)
    List<Task> findStaleTasks(@Param("userId") Long userId, @Param("staleThreshold") Instant staleThreshold);

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.domain
        WHERE t.user.id = :userId
          AND t.status = 'PENDING'
          AND t.parentTask IS NULL
        ORDER BY t.createdAt ASC
        """)
    List<Task> findAllPendingForUser(@Param("userId") Long userId);
}

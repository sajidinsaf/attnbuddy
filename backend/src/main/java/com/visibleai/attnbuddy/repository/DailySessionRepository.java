package com.visibleai.attnbuddy.repository;

import com.visibleai.attnbuddy.model.DailySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySessionRepository extends JpaRepository<DailySession, Long> {

    Optional<DailySession> findByTaskIdAndSessionDate(Long taskId, LocalDate date);

    List<DailySession> findByUserIdAndSessionDateAndStatus(Long userId, LocalDate date, DailySession.Status status);

    @Query("""
        SELECT COALESCE(SUM(s.minutesLogged), 0)
        FROM DailySession s
        WHERE s.task.id = :taskId AND s.status = 'DONE'
        """)
    int totalMinutesLogged(@Param("taskId") Long taskId);

    @Query("""
        SELECT COUNT(s)
        FROM DailySession s
        WHERE s.task.id = :taskId AND s.status = 'DONE'
        """)
    int completedSessionCount(@Param("taskId") Long taskId);
}

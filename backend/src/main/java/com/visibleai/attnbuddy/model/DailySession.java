package com.visibleai.attnbuddy.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_session", indexes = {
    @Index(name = "idx_session_task_date", columnList = "task_id, session_date", unique = true),
    @Index(name = "idx_session_user_date", columnList = "user_id, session_date, status")
})
public class DailySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "effort_minutes", nullable = false)
    private int effortMinutes;

    @Column(name = "minutes_logged", nullable = false)
    private int minutesLogged = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public enum Status { PENDING, DONE, SKIPPED }

    protected DailySession() {}

    public DailySession(Task task, User user, LocalDate sessionDate, int effortMinutes) {
        this.task = task;
        this.user = user;
        this.sessionDate = sessionDate;
        this.effortMinutes = effortMinutes;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void markDone(int minutesLogged) {
        this.status = Status.DONE;
        this.minutesLogged = minutesLogged;
        this.completedAt = Instant.now();
    }

    public void markDone() {
        markDone(this.effortMinutes);
    }

    // Getters
    public Long getId() { return id; }
    public Task getTask() { return task; }
    public User getUser() { return user; }
    public LocalDate getSessionDate() { return sessionDate; }
    public int getEffortMinutes() { return effortMinutes; }
    public int getMinutesLogged() { return minutesLogged; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}

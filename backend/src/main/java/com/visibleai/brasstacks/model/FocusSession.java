package com.visibleai.brasstacks.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "focus_session")
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "actual_minutes", nullable = false)
    private int actualMinutes;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    protected FocusSession() {}

    public FocusSession(Task task, User user, int durationMinutes, int actualMinutes,
                        boolean completed, Instant startedAt) {
        this.task = task;
        this.user = user;
        this.durationMinutes = durationMinutes;
        this.actualMinutes = actualMinutes;
        this.completed = completed;
        this.startedAt = startedAt;
        this.endedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Task getTask() { return task; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getActualMinutes() { return actualMinutes; }
    public boolean isCompleted() { return completed; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
}

package com.visibleai.attnbuddy.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task", indexes = {
    @Index(name = "idx_task_user_status", columnList = "user_id, status"),
    @Index(name = "idx_task_user_due", columnList = "user_id, due_date"),
    @Index(name = "idx_task_snoozed", columnList = "snoozed_until"),
    @Index(name = "idx_task_domain", columnList = "domain_id")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private LifeDomain domain;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urgency urgency = Urgency.NOT_URGENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Importance importance = Importance.IMPORTANT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "snoozed_until")
    private Instant snoozedUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType = TaskType.ONE_TIME;

    @Column(name = "daily_effort_minutes")
    private Integer dailyEffortMinutes;

    @Column(name = "target_date")
    private Instant targetDate;

    @Column(name = "skip_count", nullable = false)
    private int skipCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public enum TaskType { ONE_TIME, SUSTAINED }
    public enum Urgency { URGENT, NOT_URGENT }
    public enum Importance { IMPORTANT, NOT_IMPORTANT }
    public enum Status { PENDING, DONE, SKIPPED, SNOOZED }

    protected Task() {}

    public Task(User user, String title) {
        this.user = user;
        this.title = title;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void markDone() {
        this.status = Status.DONE;
        this.completedAt = Instant.now();
    }

    public void skip() {
        this.skipCount++;
    }

    public void snooze(Instant until) {
        this.status = Status.SNOOZED;
        this.snoozedUntil = until;
    }

    public void unsnooze() {
        this.status = Status.PENDING;
        this.snoozedUntil = null;
    }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public LifeDomain getDomain() { return domain; }
    public String getTitle() { return title; }
    public String getNotes() { return notes; }
    public Urgency getUrgency() { return urgency; }
    public Importance getImportance() { return importance; }
    public Status getStatus() { return status; }
    public Instant getDueDate() { return dueDate; }
    public Instant getSnoozedUntil() { return snoozedUntil; }
    public int getSkipCount() { return skipCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }

    // Setters
    public void setDomain(LifeDomain domain) { this.domain = domain; }
    public void setTitle(String title) { this.title = title; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setUrgency(Urgency urgency) { this.urgency = urgency; }
    public void setImportance(Importance importance) { this.importance = importance; }
    public void setStatus(Status status) { this.status = status; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public void setTaskType(TaskType taskType) { this.taskType = taskType; }
    public void setDailyEffortMinutes(Integer dailyEffortMinutes) { this.dailyEffortMinutes = dailyEffortMinutes; }
    public void setTargetDate(Instant targetDate) { this.targetDate = targetDate; }

    public TaskType getTaskType() { return taskType; }
    public Integer getDailyEffortMinutes() { return dailyEffortMinutes; }
    public Instant getTargetDate() { return targetDate; }

    public boolean isSustained() { return taskType == TaskType.SUSTAINED; }
}

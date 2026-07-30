package com.visibleai.brasstacks.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "goal")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "target_date")
    private Instant targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public enum Status { ACTIVE, COMPLETED }

    protected Goal() {}

    public Goal(User user, String title, Instant targetDate) {
        this.user = user;
        this.title = title;
        this.targetDate = targetDate;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Instant getTargetDate() { return targetDate; }
    public void setTargetDate(Instant targetDate) { this.targetDate = targetDate; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}

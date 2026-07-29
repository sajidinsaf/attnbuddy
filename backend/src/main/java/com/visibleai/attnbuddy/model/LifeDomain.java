package com.visibleai.attnbuddy.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "life_domain")
public class LifeDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 7)
    private String color = "#6366F1";

    @Column(nullable = false)
    private int weight = 50;

    @Column(name = "active_start")
    private LocalTime activeStart;

    @Column(name = "active_end")
    private LocalTime activeEnd;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LifeDomain() {}

    public LifeDomain(User user, String name, String color, int weight, int position) {
        this.user = user;
        this.name = name;
        this.color = color;
        this.weight = weight;
        this.position = position;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public int getWeight() { return weight; }
    public LocalTime getActiveStart() { return activeStart; }
    public LocalTime getActiveEnd() { return activeEnd; }
    public int getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setActiveStart(LocalTime activeStart) { this.activeStart = activeStart; }
    public void setActiveEnd(LocalTime activeEnd) { this.activeEnd = activeEnd; }
    public void setPosition(int position) { this.position = position; }
}

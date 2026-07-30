package com.visibleai.brasstacks.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "push_token")
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PushToken() {}

    public PushToken(User user, String token, String deviceName) {
        this.user = user;
        this.token = token;
        this.deviceName = deviceName;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getToken() { return token; }
    public String getDeviceName() { return deviceName; }
    public void setUser(User user) { this.user = user; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}

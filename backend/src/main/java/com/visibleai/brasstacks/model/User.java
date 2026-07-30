package com.visibleai.brasstacks.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Profile profile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "nudge_frequency", nullable = false)
    private NudgeFrequency nudgeFrequency = NudgeFrequency.MODERATE;

    @Column(name = "quiet_start", length = 5)
    private String quietStart;

    @Column(name = "quiet_end", length = 5)
    private String quietEnd;

    public enum Profile {
        EXECUTIVE, PROFESSIONAL, STUDENT
    }

    public enum NudgeFrequency {
        EAGER, MODERATE, MINIMAL
    }

    protected User() {}

    public User(String email, String passwordHash, String displayName, Profile profile) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.profile = profile;
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
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public Profile getProfile() { return profile; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public NudgeFrequency getNudgeFrequency() { return nudgeFrequency; }
    public void setNudgeFrequency(NudgeFrequency nudgeFrequency) { this.nudgeFrequency = nudgeFrequency; }
    public String getQuietStart() { return quietStart; }
    public void setQuietStart(String quietStart) { this.quietStart = quietStart; }
    public String getQuietEnd() { return quietEnd; }
    public void setQuietEnd(String quietEnd) { this.quietEnd = quietEnd; }

    public boolean isInQuietHours() {
        if (quietStart == null || quietEnd == null) return false;
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(quietStart);
        LocalTime end = LocalTime.parse(quietEnd);
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        // Wraps midnight (e.g., 22:00 - 07:00)
        return !now.isBefore(start) || now.isBefore(end);
    }
}

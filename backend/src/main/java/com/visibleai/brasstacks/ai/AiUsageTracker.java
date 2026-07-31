package com.visibleai.brasstacks.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class AiUsageTracker {

    private final JdbcTemplate jdbc;

    @Value("${ai.daily-limit:50}")
    private int dailyLimit;

    @Value("${ai.hourly-limit:10}")
    private int hourlyLimit;

    @Value("${ai.global-enabled:true}")
    private boolean globalEnabled;

    public AiUsageTracker(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isGlobalEnabled() {
        return globalEnabled;
    }

    public boolean canUse(Long userId) {
        if (!globalEnabled) return false;

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        int dailyCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ai_usage WHERE user_id = ? AND used_at > ?",
                Integer.class, userId, java.sql.Timestamp.from(oneDayAgo));

        if (dailyCount >= dailyLimit) return false;

        int hourlyCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ai_usage WHERE user_id = ? AND used_at > ?",
                Integer.class, userId, java.sql.Timestamp.from(oneHourAgo));

        return hourlyCount < hourlyLimit;
    }

    public void recordUsage(Long userId, String provider) {
        jdbc.update("INSERT INTO ai_usage (user_id, provider, used_at) VALUES (?, ?, ?)",
                userId, provider, java.sql.Timestamp.from(Instant.now()));
    }
}

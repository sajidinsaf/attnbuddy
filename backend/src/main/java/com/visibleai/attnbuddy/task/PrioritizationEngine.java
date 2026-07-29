package com.visibleai.attnbuddy.task;

import com.visibleai.attnbuddy.model.LifeDomain;
import com.visibleai.attnbuddy.model.Task;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PrioritizationEngine {

    public record ScoredTask(Task task, int score) {}

    public Optional<ScoredTask> pickNext(List<Task> pendingTasks) {
        Instant now = Instant.now();
        LocalTime currentTime = LocalTime.now(ZoneId.systemDefault());

        return pendingTasks.stream()
                .map(task -> new ScoredTask(task, calculateScore(task, now, currentTime)))
                .max(Comparator.comparingInt(ScoredTask::score));
    }

    public int calculateScore(Task task, Instant now, LocalTime currentTime) {
        int score = eisenhowerBase(task);
        score += deadlineBonus(task, now);
        score += stalenessBonus(task, now);
        score += domainWeight(task);
        score += domainTimeMatch(task, currentTime);
        score -= skipPenalty(task);
        return score;
    }

    private int eisenhowerBase(Task task) {
        boolean urgent = task.getUrgency() == Task.Urgency.URGENT;
        boolean important = task.getImportance() == Task.Importance.IMPORTANT;

        if (urgent && important) return 100;       // Q1
        if (!urgent && important) return 70;       // Q2
        if (urgent && !important) return 50;       // Q3
        return 20;                                  // Q4
    }

    private int deadlineBonus(Task task, Instant now) {
        if (task.getDueDate() == null) return 0;

        long hoursUntilDue = Duration.between(now, task.getDueDate()).toHours();
        if (hoursUntilDue < 0) return 40;      // overdue
        if (hoursUntilDue <= 24) return 30;
        if (hoursUntilDue <= 72) return 15;
        return 0;
    }

    private int stalenessBonus(Task task, Instant now) {
        long daysOld = Duration.between(task.getCreatedAt(), now).toDays();
        return daysOld > 3 ? 10 : 0;
    }

    private int domainWeight(Task task) {
        LifeDomain domain = task.getDomain();
        if (domain == null) return 0;
        return (int) (domain.getWeight() * 0.2); // 1-100 mapped to 0-20
    }

    private int domainTimeMatch(Task task, LocalTime currentTime) {
        LifeDomain domain = task.getDomain();
        if (domain == null || domain.getActiveStart() == null || domain.getActiveEnd() == null) {
            return 0;
        }

        boolean inWindow = !currentTime.isBefore(domain.getActiveStart())
                && !currentTime.isAfter(domain.getActiveEnd());
        return inWindow ? 15 : -10;
    }

    private int skipPenalty(Task task) {
        return task.getSkipCount() * 5; // -5 per skip, decays importance temporarily
    }
}

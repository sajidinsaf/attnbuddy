package com.visibleai.brasstacks.task;

import com.visibleai.brasstacks.model.LifeDomain;
import com.visibleai.brasstacks.model.Task;
import com.visibleai.brasstacks.repository.TaskRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class PrioritizationEngine {

    private final TaskRepository taskRepository;

    public PrioritizationEngine(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public record ScoredTask(Task task, int score) {}

    public Optional<ScoredTask> pickNext(List<Task> pendingTasks) {
        Instant now = Instant.now();
        LocalTime currentTime = LocalTime.now(ZoneId.systemDefault());
        Set<Integer> peakHours = Collections.emptySet();

        if (!pendingTasks.isEmpty()) {
            Long userId = pendingTasks.get(0).getUser().getId();
            peakHours = getPeakHours(userId);
        }

        Set<Integer> finalPeakHours = peakHours;
        return pendingTasks.stream()
                .map(task -> new ScoredTask(task, calculateScore(task, now, currentTime, finalPeakHours)))
                .max(Comparator.comparingInt(ScoredTask::score));
    }

    public List<Task> topN(List<Task> tasks, int n) {
        Instant now = Instant.now();
        LocalTime currentTime = LocalTime.now(ZoneId.systemDefault());
        Set<Integer> peakHours = Collections.emptySet();

        if (!tasks.isEmpty()) {
            Long userId = tasks.get(0).getUser().getId();
            peakHours = getPeakHours(userId);
        }

        Set<Integer> finalPeakHours = peakHours;
        return tasks.stream()
                .sorted((a, b) -> calculateScore(b, now, currentTime, finalPeakHours)
                        - calculateScore(a, now, currentTime, finalPeakHours))
                .limit(n)
                .toList();
    }

    public int calculateScore(Task task, Instant now, LocalTime currentTime, Set<Integer> peakHours) {
        int score = eisenhowerBase(task);
        score += deadlineBonus(task, now);
        score += stalenessBonus(task, now);
        score += domainWeight(task);
        score += domainTimeMatch(task, currentTime);
        score += energyPatternBonus(currentTime, peakHours);
        score -= skipPenalty(task);
        return score;
    }

    private Set<Integer> getPeakHours(Long userId) {
        if (taskRepository == null) return Collections.emptySet();
        Instant twoWeeksAgo = Instant.now().minus(Duration.ofDays(14));
        long completedCount = taskRepository.countCompletedSince(userId, twoWeeksAgo);
        if (completedCount < 10) return Collections.emptySet(); // not enough data

        List<Object[]> patterns = taskRepository.findCompletionPatternByHour(userId, twoWeeksAgo);
        Set<Integer> peak = new HashSet<>();
        // Top 3 most productive hours
        for (int i = 0; i < Math.min(3, patterns.size()); i++) {
            peak.add(((Number) patterns.get(i)[0]).intValue());
        }
        return peak;
    }

    private int energyPatternBonus(LocalTime currentTime, Set<Integer> peakHours) {
        if (peakHours.isEmpty()) return 0;
        return peakHours.contains(currentTime.getHour()) ? 15 : 0;
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

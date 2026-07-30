package com.visibleai.brasstacks.notification;

import com.visibleai.brasstacks.model.Task;
import com.visibleai.brasstacks.model.User;
import com.visibleai.brasstacks.repository.TaskRepository;
import com.visibleai.brasstacks.repository.UserRepository;
import com.visibleai.brasstacks.task.PrioritizationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NudgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(NudgeScheduler.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final PrioritizationEngine prioritizationEngine;

    public NudgeScheduler(UserRepository userRepository, TaskRepository taskRepository,
                          NotificationService notificationService,
                          PrioritizationEngine prioritizationEngine) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.prioritizationEngine = prioritizationEngine;
    }

    // Morning briefing — runs at 8 AM every day
    @Scheduled(cron = "0 0 8 * * *")
    public void morningBriefing() {
        log.info("Running morning briefing nudge");
        for (User user : userRepository.findAll()) {
            if (user.isInQuietHours()) continue;
            if (user.getNudgeFrequency() == User.NudgeFrequency.MINIMAL) continue;

            List<Task> pending = taskRepository.findAllPendingForUser(user.getId());
            if (pending.isEmpty()) continue;

            List<Task> top3 = prioritizationEngine.topN(pending, 3);
            String titles = top3.stream()
                    .map(Task::getTitle)
                    .collect(Collectors.joining(", "));

            notificationService.sendToUser(user.getId(),
                    "Your priorities today",
                    titles);
        }
    }

    // Deadline approaching — runs every 2 hours
    @Scheduled(cron = "0 0 */2 * * *")
    public void deadlineNudge() {
        log.info("Running deadline nudge");
        Instant now = Instant.now();
        Instant horizon = now.plus(Duration.ofHours(24));

        for (User user : userRepository.findAll()) {
            if (user.isInQuietHours()) continue;

            List<Task> approaching = taskRepository.findTasksWithDeadlineBetween(
                    user.getId(), now, horizon);
            if (approaching.isEmpty()) continue;

            Task urgent = approaching.get(0);
            long hoursLeft = Duration.between(now, urgent.getDueDate()).toHours();
            String timeLeft = hoursLeft < 1 ? "less than an hour" :
                    hoursLeft == 1 ? "1 hour" : hoursLeft + " hours";

            notificationService.sendToUser(user.getId(),
                    "Deadline approaching",
                    urgent.getTitle() + " — due in " + timeLeft);
        }
    }

    // Staleness — runs at noon every day
    @Scheduled(cron = "0 0 12 * * *")
    public void stalenessNudge() {
        log.info("Running staleness nudge");
        Instant staleThreshold = Instant.now().minus(Duration.ofDays(5));

        for (User user : userRepository.findAll()) {
            if (user.isInQuietHours()) continue;
            if (user.getNudgeFrequency() == User.NudgeFrequency.MINIMAL) continue;

            List<Task> stale = taskRepository.findStaleTasks(user.getId(), staleThreshold);
            if (stale.isEmpty()) continue;

            Task oldest = stale.get(0);
            long days = Duration.between(oldest.getCreatedAt(), Instant.now()).toDays();

            notificationService.sendToUser(user.getId(),
                    "Forgotten task?",
                    "You haven't touched \"" + oldest.getTitle() + "\" in " + days + " days");
        }
    }

    // Re-engagement — runs at 6 PM every day
    @Scheduled(cron = "0 0 18 * * *")
    public void reEngagement() {
        log.info("Running re-engagement nudge");
        for (User user : userRepository.findAll()) {
            if (user.isInQuietHours()) continue;
            if (user.getNudgeFrequency() != User.NudgeFrequency.EAGER) continue;

            List<Task> pending = taskRepository.findAllPendingForUser(user.getId());
            if (pending.isEmpty()) continue;

            // Find a quick win — shortest title as a proxy for simplicity
            Task quickWin = pending.stream()
                    .min((a, b) -> a.getTitle().length() - b.getTitle().length())
                    .orElse(pending.get(0));

            notificationService.sendToUser(user.getId(),
                    "One quick win for today",
                    quickWin.getTitle());
        }
    }
}

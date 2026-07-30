package com.visibleai.brasstacks.task;

import com.visibleai.brasstacks.model.LifeDomain;
import com.visibleai.brasstacks.model.Task;
import com.visibleai.brasstacks.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizationEngineTest {

    private PrioritizationEngine engine;
    private User testUser;

    @BeforeEach
    void setUp() {
        engine = new PrioritizationEngine(null);
        testUser = new User("test@example.com", "hash", "Test", User.Profile.EXECUTIVE);
    }

    @Test
    void pickNext_emptyList_returnsEmpty() {
        Optional<PrioritizationEngine.ScoredTask> result = engine.pickNext(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void pickNext_urgentImportant_scoresHigherThanNotUrgentImportant() {
        Task q1 = createTask("Q1 task", Task.Urgency.URGENT, Task.Importance.IMPORTANT);
        Task q2 = createTask("Q2 task", Task.Urgency.NOT_URGENT, Task.Importance.IMPORTANT);

        var result = engine.pickNext(List.of(q1, q2));
        assertTrue(result.isPresent());
        assertEquals("Q1 task", result.get().task().getTitle());
        assertTrue(result.get().score() > 70); // Q1 base = 100
    }

    @Test
    void pickNext_notUrgentImportant_scoresHigherThanUrgentNotImportant() {
        Task q2 = createTask("Q2 task", Task.Urgency.NOT_URGENT, Task.Importance.IMPORTANT);
        Task q3 = createTask("Q3 task", Task.Urgency.URGENT, Task.Importance.NOT_IMPORTANT);

        var result = engine.pickNext(List.of(q2, q3));
        assertTrue(result.isPresent());
        assertEquals("Q2 task", result.get().task().getTitle());
    }

    @Test
    void deadlineBonus_dueSoon_boostsScore() {
        Task withDeadline = createTask("Deadline", Task.Urgency.NOT_URGENT, Task.Importance.NOT_IMPORTANT);
        withDeadline.setDueDate(Instant.now().plus(Duration.ofHours(12)));

        Task noDeadline = createTask("No deadline", Task.Urgency.NOT_URGENT, Task.Importance.NOT_IMPORTANT);

        var result = engine.pickNext(List.of(withDeadline, noDeadline));
        assertTrue(result.isPresent());
        assertEquals("Deadline", result.get().task().getTitle());
    }

    @Test
    void skipPenalty_reducesScore() {
        Task skipped = createTask("Skipped", Task.Urgency.URGENT, Task.Importance.IMPORTANT);
        skipped.skip();
        skipped.skip();
        skipped.skip(); // 3 skips = -15 penalty

        Task fresh = createTask("Fresh", Task.Urgency.URGENT, Task.Importance.IMPORTANT);

        var result = engine.pickNext(List.of(skipped, fresh));
        assertTrue(result.isPresent());
        assertEquals("Fresh", result.get().task().getTitle());
    }

    @Test
    void domainWeight_highWeight_boostsScore() {
        LifeDomain highWeightDomain = new LifeDomain(testUser, "Work", "#6366F1", 100, 0);
        LifeDomain lowWeightDomain = new LifeDomain(testUser, "Personal", "#14B8A6", 10, 1);

        Task highWeight = createTask("High weight", Task.Urgency.NOT_URGENT, Task.Importance.IMPORTANT);
        highWeight.setDomain(highWeightDomain);

        Task lowWeight = createTask("Low weight", Task.Urgency.NOT_URGENT, Task.Importance.IMPORTANT);
        lowWeight.setDomain(lowWeightDomain);

        var result = engine.pickNext(List.of(highWeight, lowWeight));
        assertTrue(result.isPresent());
        assertEquals("High weight", result.get().task().getTitle());
    }

    private Task createTask(String title, Task.Urgency urgency, Task.Importance importance) {
        Task task = new Task(testUser, title);
        task.setUrgency(urgency);
        task.setImportance(importance);
        // Set createdAt via reflection since @PrePersist won't fire outside JPA
        try {
            Field createdAtField = Task.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(task, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return task;
    }
}

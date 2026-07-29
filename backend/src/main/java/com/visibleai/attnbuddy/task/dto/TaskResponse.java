package com.visibleai.attnbuddy.task.dto;

import com.visibleai.attnbuddy.model.Task;
import java.time.Instant;

public record TaskResponse(
        Long id, String title, String notes,
        Task.Urgency urgency, Task.Importance importance,
        Task.Status status, Instant dueDate, Instant snoozedUntil,
        Instant createdAt, Instant completedAt,
        Long domainId, String domainName, String domainColor,
        Integer score
) {
    public static TaskResponse from(Task t) {
        return from(t, null);
    }

    public static TaskResponse from(Task t, Integer score) {
        return new TaskResponse(
                t.getId(), t.getTitle(), t.getNotes(),
                t.getUrgency(), t.getImportance(),
                t.getStatus(), t.getDueDate(), t.getSnoozedUntil(),
                t.getCreatedAt(), t.getCompletedAt(),
                t.getDomain() != null ? t.getDomain().getId() : null,
                t.getDomain() != null ? t.getDomain().getName() : null,
                t.getDomain() != null ? t.getDomain().getColor() : null,
                score
        );
    }
}

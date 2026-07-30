package com.visibleai.brasstacks.task.dto;

import com.visibleai.brasstacks.model.Task;
import java.time.Instant;
import java.util.List;

public record TaskResponse(
        Long id, String title, String notes,
        Task.Urgency urgency, Task.Importance importance,
        Task.Status status, Instant dueDate, Instant snoozedUntil,
        Instant createdAt, Instant completedAt,
        Long domainId, String domainName, String domainColor,
        Integer score,
        Task.TaskType taskType,
        Integer dailyEffortMinutes, Instant targetDate,
        SustainedProgress progress,
        Long parentTaskId,
        List<MicroStepResponse> microSteps
) {
    public record SustainedProgress(
            int completedSessions, int totalMinutesLogged,
            int estimatedTotalMinutes, double percentComplete
    ) {}

    public record MicroStepResponse(Long id, String title, Task.Status status, int position) {}

    public static TaskResponse from(Task t) {
        return from(t, null, null, null);
    }

    public static TaskResponse from(Task t, Integer score, SustainedProgress progress,
                                     List<MicroStepResponse> microSteps) {
        return new TaskResponse(
                t.getId(), t.getTitle(), t.getNotes(),
                t.getUrgency(), t.getImportance(),
                t.getStatus(), t.getDueDate(), t.getSnoozedUntil(),
                t.getCreatedAt(), t.getCompletedAt(),
                t.getDomain() != null ? t.getDomain().getId() : null,
                t.getDomain() != null ? t.getDomain().getName() : null,
                t.getDomain() != null ? t.getDomain().getColor() : null,
                score,
                t.getTaskType(),
                t.getDailyEffortMinutes(), t.getTargetDate(),
                progress,
                t.getParentTask() != null ? t.getParentTask().getId() : null,
                microSteps
        );
    }
}

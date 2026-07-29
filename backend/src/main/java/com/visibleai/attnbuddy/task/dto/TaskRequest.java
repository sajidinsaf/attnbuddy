package com.visibleai.attnbuddy.task.dto;

import com.visibleai.attnbuddy.model.Task;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record TaskRequest(
        @NotBlank String title,
        String notes,
        Task.Urgency urgency,
        Task.Importance importance,
        Long domainId,
        Instant dueDate
) {}

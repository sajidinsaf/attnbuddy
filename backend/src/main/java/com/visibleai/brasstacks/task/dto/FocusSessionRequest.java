package com.visibleai.brasstacks.task.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record FocusSessionRequest(
        @Min(1) int durationMinutes,
        @Min(0) int actualMinutes,
        boolean completed,
        @NotNull Instant startedAt
) {}

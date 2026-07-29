package com.visibleai.attnbuddy.task.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record SnoozeRequest(
        @NotNull @Future Instant until
) {}

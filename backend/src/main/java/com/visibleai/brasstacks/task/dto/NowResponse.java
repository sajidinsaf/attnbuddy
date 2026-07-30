package com.visibleai.brasstacks.task.dto;

public record NowResponse(
        TaskResponse task,
        long pendingCount
) {}

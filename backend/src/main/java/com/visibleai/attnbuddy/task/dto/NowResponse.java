package com.visibleai.attnbuddy.task.dto;

public record NowResponse(
        TaskResponse task,
        long pendingCount
) {}

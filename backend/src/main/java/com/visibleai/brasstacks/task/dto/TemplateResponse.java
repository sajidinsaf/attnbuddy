package com.visibleai.brasstacks.task.dto;

import java.util.List;

public record TemplateResponse(
        String id,
        String name,
        String category,
        List<String> steps
) {}

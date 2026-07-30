package com.visibleai.brasstacks.domain.dto;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public record DomainRequest(
        @NotBlank String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex color code") String color,
        @Min(1) @Max(100) int weight,
        LocalTime activeStart,
        LocalTime activeEnd
) {}

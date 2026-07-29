package com.visibleai.attnbuddy.domain.dto;

import com.visibleai.attnbuddy.model.LifeDomain;
import java.time.LocalTime;

public record DomainResponse(
        Long id, String name, String color, int weight,
        LocalTime activeStart, LocalTime activeEnd, int position
) {
    public static DomainResponse from(LifeDomain d) {
        return new DomainResponse(d.getId(), d.getName(), d.getColor(),
                d.getWeight(), d.getActiveStart(), d.getActiveEnd(), d.getPosition());
    }
}

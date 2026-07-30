package com.visibleai.brasstacks.config;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) {
}

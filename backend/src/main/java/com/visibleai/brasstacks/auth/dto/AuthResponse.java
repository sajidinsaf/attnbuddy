package com.visibleai.brasstacks.auth.dto;

public record AuthResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        int expiresIn
) {}

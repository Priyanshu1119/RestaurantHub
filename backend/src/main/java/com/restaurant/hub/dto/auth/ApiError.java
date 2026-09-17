package com.restaurant.hub.dto.auth;

import java.time.Instant;

public record ApiError(
        boolean success,
        String message,
        Instant timestamp,
        String path
) {
    public static ApiError of(String message, String path) {
        return new ApiError(false, message, Instant.now(), path);
    }
}

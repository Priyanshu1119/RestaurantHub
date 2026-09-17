package com.restaurant.hub.dto.auth;

import com.restaurant.hub.enums.RoleName;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String name,
        String email,
        RoleName role
) {
    public static AuthResponse of(String accessToken, Long userId, String name, String email, RoleName role) {
        return new AuthResponse(accessToken, "Bearer", userId, name, email, role);
    }
}

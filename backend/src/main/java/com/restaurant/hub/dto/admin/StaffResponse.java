package com.restaurant.hub.dto.admin;

import com.restaurant.hub.entity.User;

public record StaffResponse(
        Long id,
        String name,
        String email
) {
    public static StaffResponse from(User u) {
        return new StaffResponse(u.getId(), u.getName(), u.getEmail());
    }
}

package com.restaurant.hub.dto.menu;

import com.restaurant.hub.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        Integer displayOrder,
        Long restaurantId
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDisplayOrder(), c.getRestaurant().getId());
    }
}

package com.restaurant.hub.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        Integer displayOrder,

        // Only read for SUPER_ADMIN requests. RESTAURANT_ADMIN always acts on
        // their own restaurant regardless of what's sent here.
        Long restaurantId
) {}

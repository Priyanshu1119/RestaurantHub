package com.restaurant.hub.dto.menu;

import com.restaurant.hub.enums.SpiceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @PositiveOrZero(message = "Discount price cannot be negative")
        BigDecimal discountPrice,

        @NotNull(message = "Category is required")
        Long categoryId,

        boolean vegetarian,

        SpiceLevel spiceLevel,

        List<String> ingredients,

        List<String> allergens,

        boolean available,

        Integer preparationTimeMinutes,

        boolean featured,

        boolean popular,

        // Only read for SUPER_ADMIN requests. RESTAURANT_ADMIN always acts on
        // their own restaurant regardless of what's sent here.
        Long restaurantId
) {}

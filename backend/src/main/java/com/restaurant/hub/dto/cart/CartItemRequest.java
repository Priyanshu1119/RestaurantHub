package com.restaurant.hub.dto.cart;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(

        @NotNull(message = "Menu item id is required")
        Long menuItemId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        Integer quantity
) {}

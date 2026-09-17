package com.restaurant.hub.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RestaurantUpdateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @Size(max = 250)
        String address,

        String phone,
        String email,
        String openingHours,

        @DecimalMin(value = "0.0", message = "Delivery fee cannot be negative")
        BigDecimal deliveryFee,

        @DecimalMin(value = "0.0", message = "Tax percentage cannot be negative")
        BigDecimal taxPercentage,

        @DecimalMin(value = "0.0", message = "Minimum order cannot be negative")
        BigDecimal minimumOrder
) {}

package com.restaurant.hub.dto.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseTicketRequest(

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be at least 1")
        Integer quantity
) {}

package com.restaurant.hub.dto.order;

import com.restaurant.hub.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {}

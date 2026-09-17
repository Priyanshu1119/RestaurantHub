package com.restaurant.hub.util;

import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.exception.InvalidOrderStateException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrderStatusValidator {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY));
        ALLOWED_TRANSITIONS.put(OrderStatus.READY, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY));
        ALLOWED_TRANSITIONS.put(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private OrderStatusValidator() {}

    public static void assertValidTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new InvalidOrderStateException(
                    "Cannot move an order from " + from + " to " + to);
        }
    }
}

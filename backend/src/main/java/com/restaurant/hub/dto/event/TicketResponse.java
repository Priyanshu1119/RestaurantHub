package com.restaurant.hub.dto.event;

import com.restaurant.hub.entity.Ticket;
import com.restaurant.hub.enums.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketResponse(
        Long id,
        Long eventId,
        String eventName,
        Integer quantity,
        BigDecimal totalAmount,
        TicketStatus status,
        Instant createdAt
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(t.getId(), t.getEvent().getId(), t.getEvent().getName(),
                t.getQuantity(), t.getTotalAmount(), t.getStatus(), t.getCreatedAt());
    }
}

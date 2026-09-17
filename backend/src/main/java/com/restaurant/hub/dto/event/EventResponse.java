package com.restaurant.hub.dto.event;

import com.restaurant.hub.entity.Event;
import com.restaurant.hub.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventResponse(
        Long id,
        Long restaurantId,
        String name,
        String description,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String location,
        String imageUrl,
        BigDecimal ticketPrice,
        Integer totalCapacity,
        Integer remainingSeats,
        EventStatus status
) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getRestaurant().getId(), e.getName(), e.getDescription(),
                e.getEventDate(), e.getStartTime(), e.getEndTime(), e.getLocation(), e.getImageUrl(),
                e.getTicketPrice(), e.getTotalCapacity(), e.getRemainingSeats(), e.getStatus());
    }
}

package com.restaurant.hub.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequest(

        @NotBlank(message = "Event name is required")
        String name,

        String description,

        @NotNull(message = "Event date is required")
        LocalDate eventDate,

        LocalTime startTime,
        LocalTime endTime,
        String location,

        @NotNull(message = "Ticket price is required")
        @Positive(message = "Ticket price must be greater than zero")
        BigDecimal ticketPrice,

        @NotNull(message = "Total capacity is required")
        @Positive(message = "Total capacity must be greater than zero")
        Integer totalCapacity,

        // Only read for SUPER_ADMIN requests.
        Long restaurantId
) {}

package com.restaurant.hub.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(

        @NotNull(message = "restaurantId is required")
        Long restaurantId,

        // Omit to start a new conversation.
        Long sessionId,

        @NotBlank(message = "message is required")
        String message
) {}

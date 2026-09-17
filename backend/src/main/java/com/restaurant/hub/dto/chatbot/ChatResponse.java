package com.restaurant.hub.dto.chatbot;

public record ChatResponse(
        Long sessionId,
        String reply
) {}

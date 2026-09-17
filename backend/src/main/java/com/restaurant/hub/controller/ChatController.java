package com.restaurant.hub.controller;

import com.restaurant.hub.dto.chatbot.ChatRequest;
import com.restaurant.hub.dto.chatbot.ChatResponse;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.AiCustomerCareService;
import com.restaurant.hub.util.SimpleRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-care")
@Tag(name = "Customer Care Chatbot")
public class ChatController {

    private final AiCustomerCareService aiCustomerCareService;
    private final SimpleRateLimiter rateLimiter;
    private final CurrentUserProvider currentUserProvider;

    public ChatController(AiCustomerCareService aiCustomerCareService, SimpleRateLimiter rateLimiter,
                           CurrentUserProvider currentUserProvider) {
        this.aiCustomerCareService = aiCustomerCareService;
        this.rateLimiter = rateLimiter;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the restaurant's AI customer care bot")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = currentUserProvider.getCurrentUser().getId();
        if (!rateLimiter.allow(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many messages. Please wait a moment before trying again.");
        }
        return ResponseEntity.ok(aiCustomerCareService.chat(request));
    }
}

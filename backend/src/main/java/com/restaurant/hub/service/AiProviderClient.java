package com.restaurant.hub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Talks to whichever LLM provider is configured via environment variables.
 * Kept deliberately provider-agnostic: this targets the OpenAI-compatible
 * chat-completions shape that Groq, OpenAI, and several others all speak, so
 * swapping providers is a config change, not a code change. The API key
 * never leaves this backend; React never sees it and never calls this
 * directly (see AiCustomerCareService).
 */
@Component
public class AiProviderClient {

    private final RestTemplate restTemplate;

    @Value("${app.ai.base-url}")
    private String baseUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    public AiProviderClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String chat(List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Customer care is temporarily unavailable. Please call the restaurant directly.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.3,
                "max_tokens", 400
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    baseUrl + "/chat/completions", new HttpEntity<>(body, headers), Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            return (String) messageObj.get("content");
        } catch (Exception e) {
            return "I'm having trouble answering right now. Please try again in a moment, or contact the restaurant directly.";
        }
    }
}

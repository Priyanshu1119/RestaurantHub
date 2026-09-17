package com.restaurant.hub.dto.marketing;

import com.restaurant.hub.enums.CustomerSegment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CampaignRequest(

        @NotBlank(message = "Subject is required")
        String subject,

        @NotBlank(message = "Content is required")
        String content,

        @NotNull(message = "Segment is required")
        CustomerSegment segment,

        Instant scheduledAt,

        // Only read for SUPER_ADMIN requests.
        Long restaurantId
) {}

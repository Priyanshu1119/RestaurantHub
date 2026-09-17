package com.restaurant.hub.dto.marketing;

import com.restaurant.hub.entity.MarketingCampaign;
import com.restaurant.hub.enums.CampaignStatus;
import com.restaurant.hub.enums.CustomerSegment;

import java.time.Instant;

public record CampaignResponse(
        Long id,
        String subject,
        String content,
        CustomerSegment segment,
        Instant scheduledAt,
        Instant sentAt,
        CampaignStatus status
) {
    public static CampaignResponse from(MarketingCampaign c) {
        return new CampaignResponse(c.getId(), c.getSubject(), c.getContent(), c.getSegment(),
                c.getScheduledAt(), c.getSentAt(), c.getStatus());
    }
}

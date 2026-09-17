package com.restaurant.hub.service;

import com.restaurant.hub.dto.marketing.CampaignRequest;
import com.restaurant.hub.dto.marketing.CampaignResponse;
import com.restaurant.hub.entity.MarketingCampaign;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.CampaignStatus;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.MarketingCampaignRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketingCampaignService {

    private final MarketingCampaignRepository campaignRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;

    public MarketingCampaignService(MarketingCampaignRepository campaignRepository,
                                     RestaurantRepository restaurantRepository,
                                     CurrentUserProvider currentUserProvider) {
        this.campaignRepository = campaignRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<CampaignResponse> listByRestaurant(Long restaurantId) {
        assertCanManage(restaurantId);
        return campaignRepository.findByRestaurantId(restaurantId).stream().map(CampaignResponse::from).toList();
    }

    @Transactional
    public CampaignResponse create(CampaignRequest request) {
        Long restaurantId = resolveRestaurantId(request.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        MarketingCampaign campaign = MarketingCampaign.builder()
                .restaurant(restaurant)
                .subject(request.subject())
                .content(request.content())
                .segment(request.segment())
                .scheduledAt(request.scheduledAt())
                .status(request.scheduledAt() != null ? CampaignStatus.SCHEDULED : CampaignStatus.DRAFT)
                .build();

        return CampaignResponse.from(campaignRepository.save(campaign));
    }

    /**
     * Marks a draft/scheduled campaign as sent. Actual email dispatch goes
     * through NotificationService (Phase 10); this only flips campaign state
     * so the marketing dashboard reflects what went out.
     */
    @Transactional
    public CampaignResponse markSent(Long campaignId) {
        MarketingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id " + campaignId));
        assertCanManage(campaign.getRestaurant().getId());
        campaign.setStatus(CampaignStatus.SENT);
        campaign.setSentAt(java.time.Instant.now());
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public void delete(Long campaignId) {
        MarketingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id " + campaignId));
        assertCanManage(campaign.getRestaurant().getId());
        campaignRepository.delete(campaign);
    }

    private Long resolveRestaurantId(Long requestedRestaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            if (requestedRestaurantId == null) {
                throw new IllegalArgumentException("restaurantId is required for super admin requests");
            }
            return requestedRestaurantId;
        }
        if (current.getRestaurant() == null) {
            throw new UnauthorizedException("Your account is not linked to a restaurant");
        }
        return current.getRestaurant().getId();
    }

    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRestaurant() == null || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this restaurant's marketing");
        }
    }
}

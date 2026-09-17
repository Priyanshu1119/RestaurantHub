package com.restaurant.hub.repository;

import com.restaurant.hub.entity.MarketingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, Long> {
    List<MarketingCampaign> findByRestaurantId(Long restaurantId);
}

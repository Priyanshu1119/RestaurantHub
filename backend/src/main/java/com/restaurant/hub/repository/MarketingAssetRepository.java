package com.restaurant.hub.repository;

import com.restaurant.hub.entity.MarketingAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketingAssetRepository extends JpaRepository<MarketingAsset, Long> {
    List<MarketingAsset> findByRestaurantId(Long restaurantId);
}

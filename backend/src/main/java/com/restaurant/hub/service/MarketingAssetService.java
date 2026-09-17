package com.restaurant.hub.service;

import com.restaurant.hub.dto.marketing.AssetResponse;
import com.restaurant.hub.entity.MarketingAsset;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.MarketingAssetRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MarketingAssetService {

    private final MarketingAssetRepository assetRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public MarketingAssetService(MarketingAssetRepository assetRepository, RestaurantRepository restaurantRepository,
                                  CurrentUserProvider currentUserProvider, ImageStorageService imageStorageService) {
        this.assetRepository = assetRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
        this.imageStorageService = imageStorageService;
    }

    public List<AssetResponse> listByRestaurant(Long restaurantId) {
        assertCanManage(restaurantId);
        return assetRepository.findByRestaurantId(restaurantId).stream().map(AssetResponse::from).toList();
    }

    @Transactional
    public AssetResponse upload(Long restaurantId, String name, String assetType, MultipartFile file) {
        assertCanManage(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        String url = imageStorageService.upload(file, "marketing-assets");
        MarketingAsset asset = MarketingAsset.builder()
                .restaurant(restaurant)
                .name(name)
                .assetType(assetType)
                .url(url)
                .build();

        return AssetResponse.from(assetRepository.save(asset));
    }

    @Transactional
    public void delete(Long assetId) {
        MarketingAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id " + assetId));
        assertCanManage(asset.getRestaurant().getId());
        assetRepository.delete(asset);
    }

    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRestaurant() == null || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this restaurant's marketing assets");
        }
    }
}

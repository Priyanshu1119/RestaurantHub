package com.restaurant.hub.service;

import com.restaurant.hub.dto.menu.RestaurantResponse;
import com.restaurant.hub.dto.menu.RestaurantUpdateRequest;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public RestaurantService(RestaurantRepository restaurantRepository,
                              CurrentUserProvider currentUserProvider,
                              ImageStorageService imageStorageService) {
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
        this.imageStorageService = imageStorageService;
    }

    public RestaurantResponse getById(Long id) {
        return RestaurantResponse.from(findOrThrow(id));
    }

    @Transactional
    public RestaurantResponse update(Long restaurantId, RestaurantUpdateRequest request) {
        Restaurant restaurant = findOrThrow(restaurantId);
        assertCanManage(restaurant.getId());

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setAddress(request.address());
        restaurant.setPhone(request.phone());
        restaurant.setEmail(request.email());
        restaurant.setOpeningHours(request.openingHours());
        if (request.deliveryFee() != null) restaurant.setDeliveryFee(request.deliveryFee());
        if (request.taxPercentage() != null) restaurant.setTaxPercentage(request.taxPercentage());
        if (request.minimumOrder() != null) restaurant.setMinimumOrder(request.minimumOrder());

        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse updateLogo(Long restaurantId, org.springframework.web.multipart.MultipartFile file) {
        Restaurant restaurant = findOrThrow(restaurantId);
        assertCanManage(restaurant.getId());
        restaurant.setLogoUrl(imageStorageService.upload(file, "restaurants/logos"));
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse updateCoverImage(Long restaurantId, org.springframework.web.multipart.MultipartFile file) {
        Restaurant restaurant = findOrThrow(restaurantId);
        assertCanManage(restaurant.getId());
        restaurant.setCoverImageUrl(imageStorageService.upload(file, "restaurants/covers"));
        return RestaurantResponse.from(restaurant);
    }

    private Restaurant findOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + id));
    }

    /** SUPER_ADMIN can manage any restaurant. RESTAURANT_ADMIN only their own. */
    private void assertCanManage(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        if (current.getRole() != RoleName.RESTAURANT_ADMIN
                || current.getRestaurant() == null
                || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this restaurant");
        }
    }
}

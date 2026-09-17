package com.restaurant.hub.service;

import com.restaurant.hub.dto.menu.CategoryRequest;
import com.restaurant.hub.dto.menu.CategoryResponse;
import com.restaurant.hub.entity.Category;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.CategoryRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;

    public CategoryService(CategoryRepository categoryRepository,
                            RestaurantRepository restaurantRepository,
                            CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<CategoryResponse> listByRestaurant(Long restaurantId) {
        return categoryRepository.findByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
                .stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Long restaurantId = resolveRestaurantId(request.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.name())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
        assertCanManage(category.getRestaurant().getId());

        category.setName(request.name());
        if (request.displayOrder() != null) category.setDisplayOrder(request.displayOrder());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
        assertCanManage(category.getRestaurant().getId());
        categoryRepository.delete(category);
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
            throw new UnauthorizedException("You do not have permission to manage this category");
        }
    }
}

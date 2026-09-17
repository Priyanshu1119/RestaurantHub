package com.restaurant.hub.service;

import com.restaurant.hub.dto.menu.MenuItemRequest;
import com.restaurant.hub.dto.menu.MenuItemResponse;
import com.restaurant.hub.entity.Category;
import com.restaurant.hub.entity.MenuItem;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.CategoryRepository;
import com.restaurant.hub.repository.MenuItemRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.specification.MenuItemSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ImageStorageService imageStorageService;

    public MenuItemService(MenuItemRepository menuItemRepository,
                            CategoryRepository categoryRepository,
                            RestaurantRepository restaurantRepository,
                            CurrentUserProvider currentUserProvider,
                            ImageStorageService imageStorageService) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.currentUserProvider = currentUserProvider;
        this.imageStorageService = imageStorageService;
    }

    public Page<MenuItemResponse> search(Long restaurantId, Long categoryId, Boolean vegetarian,
                                          String search, Pageable pageable) {
        Specification<MenuItem> spec = Specification
                .where(MenuItemSpecification.hasRestaurant(restaurantId))
                .and(MenuItemSpecification.hasCategory(categoryId))
                .and(MenuItemSpecification.isVegetarian(vegetarian))
                .and(MenuItemSpecification.isAvailable(true))
                .and(MenuItemSpecification.nameOrDescriptionContains(search));

        return menuItemRepository.findAll(spec, pageable).map(MenuItemResponse::from);
    }

    public MenuItemResponse getById(Long id) {
        return MenuItemResponse.from(findOrThrow(id));
    }

    @Transactional
    public MenuItemResponse create(MenuItemRequest request) {
        Long restaurantId = resolveRestaurantId(request.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id " + restaurantId));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + request.categoryId()));

        if (!category.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Category does not belong to this restaurant");
        }

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .discountPrice(request.discountPrice())
                .vegetarian(request.vegetarian())
                .spiceLevel(request.spiceLevel())
                .ingredients(request.ingredients())
                .allergens(request.allergens())
                .available(request.available())
                .preparationTimeMinutes(request.preparationTimeMinutes())
                .featured(request.featured())
                .popular(request.popular())
                .build();

        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = findOrThrow(id);
        assertCanManage(item.getRestaurant().getId());

        if (!item.getCategory().getId().equals(request.categoryId())) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + request.categoryId()));
            if (!category.getRestaurant().getId().equals(item.getRestaurant().getId())) {
                throw new IllegalArgumentException("Category does not belong to this restaurant");
            }
            item.setCategory(category);
        }

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setDiscountPrice(request.discountPrice());
        item.setVegetarian(request.vegetarian());
        item.setSpiceLevel(request.spiceLevel());
        item.setIngredients(request.ingredients());
        item.setAllergens(request.allergens());
        item.setAvailable(request.available());
        item.setPreparationTimeMinutes(request.preparationTimeMinutes());
        item.setFeatured(request.featured());
        item.setPopular(request.popular());

        return MenuItemResponse.from(item);
    }

    @Transactional
    public MenuItemResponse updateImage(Long id, MultipartFile file) {
        MenuItem item = findOrThrow(id);
        assertCanManage(item.getRestaurant().getId());
        item.setImageUrl(imageStorageService.upload(file, "menu-items"));
        return MenuItemResponse.from(item);
    }

    @Transactional
    public void delete(Long id) {
        MenuItem item = findOrThrow(id);
        assertCanManage(item.getRestaurant().getId());
        menuItemRepository.delete(item);
    }

    private MenuItem findOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id " + id));
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
            throw new UnauthorizedException("You do not have permission to manage this menu item");
        }
    }
}

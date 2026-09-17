package com.restaurant.hub;

import com.restaurant.hub.dto.menu.MenuItemRequest;
import com.restaurant.hub.entity.Category;
import com.restaurant.hub.entity.Restaurant;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.enums.SpiceLevel;
import com.restaurant.hub.repository.CategoryRepository;
import com.restaurant.hub.repository.MenuItemRepository;
import com.restaurant.hub.repository.RestaurantRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.ImageStorageService;
import com.restaurant.hub.service.MenuItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private MenuItemService menuItemService;

    @Test
    void create_rejectsCategoryFromAnotherRestaurant() {
        Restaurant myRestaurant = Restaurant.builder().id(1L).name("Spice House").build();
        Restaurant otherRestaurant = Restaurant.builder().id(2L).name("Other Place").build();
        Category categoryFromOtherRestaurant = Category.builder().id(10L).restaurant(otherRestaurant).name("Starters").build();

        User admin = User.builder().id(1L).role(RoleName.RESTAURANT_ADMIN).restaurant(myRestaurant).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(myRestaurant));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(categoryFromOtherRestaurant));

        MenuItemRequest request = new MenuItemRequest(
                "Paneer Tikka", "desc", new BigDecimal("249.00"), null, 10L,
                true, SpiceLevel.MEDIUM, List.of(), List.of(), true, 15, false, false, null
        );

        assertThrows(IllegalArgumentException.class, () -> menuItemService.create(request));
    }

    @Test
    void create_superAdminMustProvideRestaurantId() {
        User superAdmin = User.builder().id(2L).role(RoleName.SUPER_ADMIN).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(superAdmin);

        MenuItemRequest request = new MenuItemRequest(
                "Paneer Tikka", "desc", new BigDecimal("249.00"), null, 10L,
                true, SpiceLevel.MEDIUM, List.of(), List.of(), true, 15, false, false, null
        );

        assertThrows(IllegalArgumentException.class, () -> menuItemService.create(request));
    }

    @Test
    void create_restaurantAdminWithoutRestaurantIsUnauthorized() {
        User admin = User.builder().id(3L).role(RoleName.RESTAURANT_ADMIN).restaurant(null).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(admin);

        MenuItemRequest request = new MenuItemRequest(
                "Paneer Tikka", "desc", new BigDecimal("249.00"), null, 10L,
                true, SpiceLevel.MEDIUM, List.of(), List.of(), true, 15, false, false, null
        );

        assertThrows(com.restaurant.hub.exception.UnauthorizedException.class, () -> menuItemService.create(request));
    }
}

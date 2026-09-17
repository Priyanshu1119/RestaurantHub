package com.restaurant.hub;

import com.restaurant.hub.dto.cart.CartItemRequest;
import com.restaurant.hub.entity.*;
import com.restaurant.hub.repository.CartItemRepository;
import com.restaurant.hub.repository.CartRepository;
import com.restaurant.hub.repository.MenuItemRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private CartService cartService;

    private Restaurant restaurantA() {
        return Restaurant.builder().id(1L).name("Spice House")
                .deliveryFee(BigDecimal.TEN).taxPercentage(BigDecimal.valueOf(5)).build();
    }

    private Restaurant restaurantB() {
        return Restaurant.builder().id(2L).name("Other Place").build();
    }

    @Test
    void addItem_rejectsItemFromDifferentRestaurantThanCurrentCart() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Cart cart = Cart.builder().id(1L).user(user).restaurant(restaurantA()).items(new ArrayList<>()).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        MenuItem itemFromOtherRestaurant = MenuItem.builder().id(5L).restaurant(restaurantB())
                .name("Burger").price(BigDecimal.TEN).available(true).build();
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(itemFromOtherRestaurant));

        CartItemRequest request = new CartItemRequest(5L, 1);
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(request));
    }

    @Test
    void addItem_rejectsUnavailableItem() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        Cart cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        MenuItem soldOut = MenuItem.builder().id(6L).restaurant(restaurantA())
                .name("Sold Out Dish").price(BigDecimal.TEN).available(false).build();
        when(menuItemRepository.findById(6L)).thenReturn(Optional.of(soldOut));

        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(new CartItemRequest(6L, 1)));
    }

    @Test
    void addItem_mergesQuantityWhenSameItemAddedTwice() {
        User user = User.builder().id(1L).build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);

        MenuItem menuItem = MenuItem.builder().id(7L).restaurant(restaurantA())
                .name("Paneer Tikka").price(BigDecimal.valueOf(249)).available(true).build();

        CartItem existing = CartItem.builder().id(100L).menuItem(menuItem).quantity(2).build();
        Cart cart = Cart.builder().id(1L).user(user).restaurant(restaurantA())
                .items(new ArrayList<>(java.util.List.of(existing))).build();
        existing.setCart(cart);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(menuItemRepository.findById(7L)).thenReturn(Optional.of(menuItem));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.addItem(new CartItemRequest(7L, 3));

        assertEquals(5, existing.getQuantity());
        assertEquals(1, cart.getItems().size());
    }
}

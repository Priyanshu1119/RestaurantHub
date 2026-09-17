package com.restaurant.hub.service;

import com.restaurant.hub.dto.cart.CartItemRequest;
import com.restaurant.hub.dto.cart.CartResponse;
import com.restaurant.hub.dto.cart.UpdateQuantityRequest;
import com.restaurant.hub.entity.Cart;
import com.restaurant.hub.entity.CartItem;
import com.restaurant.hub.entity.MenuItem;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.CartItemRepository;
import com.restaurant.hub.repository.CartRepository;
import com.restaurant.hub.repository.MenuItemRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final CurrentUserProvider currentUserProvider;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                        MenuItemRepository menuItemRepository, CurrentUserProvider currentUserProvider) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public CartResponse getMyCart() {
        return CartResponse.from(getOrCreateCart());
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Cart cart = getOrCreateCart();
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id " + request.menuItemId()));

        if (!menuItem.isAvailable()) {
            throw new IllegalArgumentException("This item is currently unavailable");
        }

        if (cart.getRestaurant() == null) {
            cart.setRestaurant(menuItem.getRestaurant());
        } else if (!cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            throw new IllegalArgumentException(
                    "Your cart has items from another restaurant. Clear your cart before ordering from a new one.");
        }

        cart.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.quantity()),
                        () -> cart.getItems().add(CartItem.builder()
                                .cart(cart)
                                .menuItem(menuItem)
                                .quantity(request.quantity())
                                .build())
                );

        return CartResponse.from(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(Long cartItemId, UpdateQuantityRequest request) {
        CartItem item = findOwnedItemOrThrow(cartItemId);
        item.setQuantity(request.quantity());
        return CartResponse.from(item.getCart());
    }

    @Transactional
    public CartResponse removeItem(Long cartItemId) {
        CartItem item = findOwnedItemOrThrow(cartItemId);
        Cart cart = item.getCart();
        cart.getItems().remove(item); // orphanRemoval=true on Cart.items deletes the row on flush
        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
        }
        return CartResponse.from(cart);
    }

    @Transactional
    public void clear() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cart.setRestaurant(null);
    }

    /** Package-private so OrderService can pull the live cart during checkout. */
    Cart getOrCreateCart() {
        User current = currentUserProvider.getCurrentUser();
        return cartRepository.findByUserId(current.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(current).build()));
    }

    private CartItem findOwnedItemOrThrow(Long cartItemId) {
        User current = currentUserProvider.getCurrentUser();
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id " + cartItemId));
        if (!item.getCart().getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("This is not your cart item");
        }
        return item;
    }
}

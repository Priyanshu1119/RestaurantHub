package com.restaurant.hub.service;

import com.restaurant.hub.dto.order.CreateOrderRequest;
import com.restaurant.hub.dto.order.OrderResponse;
import com.restaurant.hub.entity.*;
import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.EmptyCartException;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.OrderRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.util.OrderStatusValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final CurrentUserProvider currentUserProvider;
    private final CouponService couponService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, CartService cartService,
                         AddressService addressService, CurrentUserProvider currentUserProvider,
                         CouponService couponService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.addressService = addressService;
        this.currentUserProvider = currentUserProvider;
        this.couponService = couponService;
        this.notificationService = notificationService;
    }

    @Transactional
    public OrderResponse createFromCart(CreateOrderRequest request) {
        User current = currentUserProvider.getCurrentUser();
        Cart cart = cartService.getOrCreateCart();

        if (cart.getItems().isEmpty() || cart.getRestaurant() == null) {
            throw new EmptyCartException("Your cart is empty");
        }

        Address address = addressService.findOwnedOrThrow(request.addressId());
        Restaurant restaurant = cart.getRestaurant();
        String deliveryAddressLine = com.restaurant.hub.dto.address.AddressResponse.from(address).toSingleLine();

        // Backend computes every amount. Nothing here comes from the client.
        BigDecimal subtotal = BigDecimal.ZERO;
        Order order = Order.builder()
                .user(current)
                .restaurant(restaurant)
                .deliveryAddress(deliveryAddressLine)
                .contactPhone(address.getPhone())
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = cartItem.getMenuItem();
            BigDecimal unitPrice = menuItem.getDiscountPrice() != null ? menuItem.getDiscountPrice() : menuItem.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            order.getItems().add(OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .itemName(menuItem.getName())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        BigDecimal taxAmount = subtotal
                .multiply(restaurant.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = restaurant.getDeliveryFee();

        BigDecimal discountAmount = BigDecimal.ZERO;
        boolean couponApplied = request.couponCode() != null && !request.couponCode().isBlank();
        if (couponApplied) {
            discountAmount = couponService.computeDiscountForCheckout(request.couponCode(), restaurant, subtotal, current);
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount).add(taxAmount).add(deliveryFee);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);
        if (couponApplied) {
            couponService.recordUsage(request.couponCode(), restaurant.getId(), current, saved);
        }
        cartService.clear();
        notificationService.notifyOrderPlaced(saved);

        return OrderResponse.from(saved);
    }

    public Page<OrderResponse> myOrders(Pageable pageable) {
        User current = currentUserProvider.getCurrentUser();
        return orderRepository.findByUserId(current.getId(), pageable).map(OrderResponse::from);
    }

    public Page<OrderResponse> restaurantOrders(Long restaurantId, OrderStatus status, Pageable pageable) {
        assertStaffOfRestaurant(restaurantId);
        Page<Order> page = status != null
                ? orderRepository.findByRestaurantIdAndStatus(restaurantId, status, pageable)
                : orderRepository.findByRestaurantId(restaurantId, pageable);
        return page.map(OrderResponse::from);
    }

    public OrderResponse getById(Long id) {
        Order order = findOrThrow(id);
        assertCanView(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = findOrThrow(id);
        assertStaffOfRestaurant(order.getRestaurant().getId());
        OrderStatusValidator.assertValidTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        notificationService.notifyOrderStatusChanged(order);
        return OrderResponse.from(order);
    }

    /**
     * Lets a customer cancel their own order while it's still eligible
     * (PENDING or CONFIRMED). Used by the customer-care chatbot's cancel
     * flow and available as a direct endpoint too, both gated on the caller
     * actually owning the order.
     */
    @Transactional
    public OrderResponse cancelOwnOrder(Long id) {
        Order order = findOrThrow(id);
        User current = currentUserProvider.getCurrentUser();
        if (!order.getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("This is not your order");
        }
        OrderStatusValidator.assertValidTransition(order.getStatus(), OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    public boolean isCancellableByCustomer(Order order) {
        return order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED;
    }

    private Order findOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private void assertCanView(Order order) {
        User current = currentUserProvider.getCurrentUser();
        boolean isOwner = order.getUser().getId().equals(current.getId());
        boolean isRestaurantStaff = current.getRestaurant() != null
                && current.getRestaurant().getId().equals(order.getRestaurant().getId());
        boolean isSuperAdmin = current.getRole() == RoleName.SUPER_ADMIN;

        if (!isOwner && !isRestaurantStaff && !isSuperAdmin) {
            throw new UnauthorizedException("You do not have permission to view this order");
        }
    }

    private void assertStaffOfRestaurant(Long restaurantId) {
        User current = currentUserProvider.getCurrentUser();
        if (current.getRole() == RoleName.SUPER_ADMIN) {
            return;
        }
        boolean isStaffOrAdmin = current.getRole() == RoleName.RESTAURANT_ADMIN
                || current.getRole() == RoleName.RESTAURANT_STAFF;
        if (!isStaffOrAdmin || current.getRestaurant() == null || !current.getRestaurant().getId().equals(restaurantId)) {
            throw new UnauthorizedException("You do not have permission to manage this restaurant's orders");
        }
    }
}

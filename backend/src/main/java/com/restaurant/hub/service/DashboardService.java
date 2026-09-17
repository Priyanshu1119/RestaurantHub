package com.restaurant.hub.service;

import com.restaurant.hub.dto.admin.DashboardResponse;
import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponRepository couponRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public DashboardService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                             CouponUsageRepository couponUsageRepository, CouponRepository couponRepository,
                             TicketRepository ticketRepository, EventRepository eventRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.couponRepository = couponRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    public DashboardResponse getDashboard(Long restaurantId) {
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS).atZone(ZoneOffset.UTC).toInstant();

        long todayOrders = orderRepository.countByRestaurantIdAndCreatedAtAfter(restaurantId, startOfToday);
        var todayRevenue = orderRepository.sumRevenueSince(restaurantId, startOfToday);

        long pendingOrders = orderRepository.countByRestaurantIdAndStatusIn(restaurantId, EnumSet.of(
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY));
        long completedOrders = orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.DELIVERED);
        long totalCustomers = orderRepository.countDistinctCustomers(restaurantId);

        List<DashboardResponse.PopularItem> popularItems = orderItemRepository
                .findPopularItems(restaurantId, PageRequest.of(0, 5))
                .stream()
                .map(p -> new DashboardResponse.PopularItem(p.getName(), p.getTotalQuantity()))
                .toList();

        long couponUsageCount = couponRepository.findByRestaurantId(restaurantId).stream()
                .mapToLong(c -> couponUsageRepository.countByCouponId(c.getId()))
                .sum();

        long eventTicketsSold = eventRepository.findByRestaurantId(restaurantId, PageRequest.of(0, 500))
                .stream()
                .mapToLong(e -> ticketRepository.countByEventIdAndStatus(e.getId(), com.restaurant.hub.enums.TicketStatus.PAID))
                .sum();

        return new DashboardResponse(todayOrders, todayRevenue, pendingOrders, completedOrders,
                totalCustomers, popularItems, couponUsageCount, eventTicketsSold);
    }
}

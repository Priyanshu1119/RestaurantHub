package com.restaurant.hub.service;

import com.restaurant.hub.dto.admin.CustomerSummaryResponse;
import com.restaurant.hub.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final OrderRepository orderRepository;

    public CustomerService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<CustomerSummaryResponse> listByRestaurant(Long restaurantId) {
        return orderRepository.findCustomerSummaries(restaurantId).stream()
                .map(p -> new CustomerSummaryResponse(p.getUserId(), p.getName(), p.getEmail(),
                        p.getTotalOrders(), p.getTotalSpent(), p.getLastOrderAt()))
                .toList();
    }
}

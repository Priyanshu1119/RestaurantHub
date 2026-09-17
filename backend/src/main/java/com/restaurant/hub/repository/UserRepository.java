package com.restaurant.hub.repository;

import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRestaurantIdAndRole(Long restaurantId, RoleName role);
}

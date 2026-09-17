package com.restaurant.hub.security;

import com.restaurant.hub.entity.User;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Loads the User behind the currently authenticated request. The JWT filter
 * only puts the email in the security context, so admin-scoped services use
 * this to find out which restaurant (if any) that user actually owns,
 * instead of trusting a restaurantId the client might send.
 */
@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}

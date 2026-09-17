package com.restaurant.hub.service;

import com.restaurant.hub.dto.auth.AuthResponse;
import com.restaurant.hub.dto.auth.LoginRequest;
import com.restaurant.hub.dto.auth.RegisterRequest;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.EmailAlreadyExistsException;
import com.restaurant.hub.exception.InvalidCredentialsException;
import com.restaurant.hub.repository.UserRepository;
import com.restaurant.hub.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(RoleName.CUSTOMER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId(), saved.getRole().name());
        log.info("New account registered, userId={}, role={}", saved.getId(), saved.getRole());

        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: no account for the given email");
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: bad password, userId={}", user.getId());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());
        log.info("Login succeeded, userId={}", user.getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

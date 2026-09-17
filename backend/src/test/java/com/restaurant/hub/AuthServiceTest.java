package com.restaurant.hub;

import com.restaurant.hub.dto.auth.LoginRequest;
import com.restaurant.hub.dto.auth.RegisterRequest;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.RoleName;
import com.restaurant.hub.exception.EmailAlreadyExistsException;
import com.restaurant.hub.exception.InvalidCredentialsException;
import com.restaurant.hub.repository.UserRepository;
import com.restaurant.hub.security.JwtUtil;
import com.restaurant.hub.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Priyanshu", "p@test.com", "password123", "9999999999");
        when(userRepository.existsByEmail("p@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_savesUserWithCustomerRoleAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Priyanshu", "p@test.com", "password123", "9999999999");
        when(userRepository.existsByEmail("p@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(eq("p@test.com"), eq(1L), eq("CUSTOMER"))).thenReturn("fake-jwt");

        var response = authService.register(request);

        assertEquals("fake-jwt", response.accessToken());
        assertEquals(RoleName.CUSTOMER, response.role());
    }

    @Test
    void login_throwsOnWrongPassword() {
        User user = User.builder().id(1L).email("p@test.com").passwordHash("hashed")
                .role(RoleName.CUSTOMER).createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(userRepository.findByEmail("p@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest("p@test.com", "wrong");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("missing@test.com", "password123");
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}

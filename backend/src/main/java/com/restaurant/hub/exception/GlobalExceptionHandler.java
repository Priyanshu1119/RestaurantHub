package com.restaurant.hub.exception;

import com.restaurant.hub.dto.auth.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ApiError> handleBadCredentials(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of("Invalid email or password", req.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<ApiError> handlePaymentVerification(PaymentVerificationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ApiError> handleInvalidOrderState(InvalidOrderStateException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ApiError> handleEmptyCart(EmptyCartException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiError> handleInvalidCoupon(InvalidCouponException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(TicketUnavailableException.class)
    public ResponseEntity<ApiError> handleTicketUnavailable(TicketUnavailableException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Validation failed");
        body.put("path", req.getRequestURI());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("Something went wrong. Please try again.", req.getRequestURI()));
    }
}

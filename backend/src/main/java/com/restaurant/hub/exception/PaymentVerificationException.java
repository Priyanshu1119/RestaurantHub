package com.restaurant.hub.exception;

public class PaymentVerificationException extends RuntimeException {
    public PaymentVerificationException(String message) {
        super(message);
    }
}

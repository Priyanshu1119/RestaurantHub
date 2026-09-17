package com.restaurant.hub.exception;

public class TicketUnavailableException extends RuntimeException {
    public TicketUnavailableException(String message) {
        super(message);
    }
}

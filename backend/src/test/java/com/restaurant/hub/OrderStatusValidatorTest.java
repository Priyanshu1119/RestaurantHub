package com.restaurant.hub;

import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.exception.InvalidOrderStateException;
import com.restaurant.hub.util.OrderStatusValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusValidatorTest {

    @Test
    void allowsTheDocumentedHappyPath() {
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.PREPARING, OrderStatus.READY));
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY));
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED));
    }

    @Test
    void rejectsSkippingAStage() {
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.PENDING, OrderStatus.PREPARING));
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.CONFIRMED, OrderStatus.READY));
    }

    @Test
    void rejectsMovingBackward() {
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.PREPARING, OrderStatus.CONFIRMED));
    }

    @Test
    void terminalStatesAcceptNoFurtherTransitions() {
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.DELIVERED, OrderStatus.CONFIRMED));
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.CANCELLED, OrderStatus.PENDING));
    }

    @Test
    void allowsCancellingBeforePreparationStarts() {
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.PENDING, OrderStatus.CANCELLED));
        assertDoesNotThrow(() -> OrderStatusValidator.assertValidTransition(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
    }

    @Test
    void disallowsCancellingOnceBeingPrepared() {
        assertThrows(InvalidOrderStateException.class,
                () -> OrderStatusValidator.assertValidTransition(OrderStatus.PREPARING, OrderStatus.CANCELLED));
    }
}

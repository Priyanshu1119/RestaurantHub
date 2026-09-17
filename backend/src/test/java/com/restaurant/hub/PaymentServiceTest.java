package com.restaurant.hub;

import com.razorpay.RazorpayClient;
import com.restaurant.hub.dto.payment.CreatePaymentRequest;
import com.restaurant.hub.entity.Order;
import com.restaurant.hub.entity.Payment;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.enums.PaymentStatus;
import com.restaurant.hub.exception.PaymentVerificationException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.OrderRepository;
import com.restaurant.hub.repository.PaymentRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentOrder_rejectsOrderThatIsNotPending() {
        User customer = User.builder().id(1L).build();
        Order order = Order.builder().id(10L).user(customer).status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(299)).build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(currentUserProvider.getCurrentUser()).thenReturn(customer);

        assertThrows(PaymentVerificationException.class,
                () -> paymentService.createPaymentOrder(new CreatePaymentRequest(10L)));
    }

    @Test
    void createPaymentOrder_rejectsAlreadyPaidOrder() {
        User customer = User.builder().id(1L).build();
        Order order = Order.builder().id(11L).user(customer).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(299)).build();
        Payment existingPayment = Payment.builder().id(1L).order(order).status(PaymentStatus.SUCCESS).build();

        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));
        when(currentUserProvider.getCurrentUser()).thenReturn(customer);
        when(paymentRepository.findByOrderId(11L)).thenReturn(Optional.of(existingPayment));

        assertThrows(PaymentVerificationException.class,
                () -> paymentService.createPaymentOrder(new CreatePaymentRequest(11L)));
    }

    @Test
    void createPaymentOrder_rejectsWhenCallerDoesNotOwnTheOrder() {
        User owner = User.builder().id(1L).build();
        User someoneElse = User.builder().id(2L).build();
        Order order = Order.builder().id(12L).user(owner).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(299)).build();

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));
        when(currentUserProvider.getCurrentUser()).thenReturn(someoneElse);

        assertThrows(UnauthorizedException.class,
                () -> paymentService.createPaymentOrder(new CreatePaymentRequest(12L)));
    }
}

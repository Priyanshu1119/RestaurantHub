package com.restaurant.hub.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.restaurant.hub.dto.payment.CreatePaymentRequest;
import com.restaurant.hub.dto.payment.CreatePaymentResponse;
import com.restaurant.hub.dto.payment.PaymentResponse;
import com.restaurant.hub.dto.payment.VerifyPaymentRequest;
import com.restaurant.hub.entity.Order;
import com.restaurant.hub.entity.Payment;
import com.restaurant.hub.entity.User;
import com.restaurant.hub.enums.OrderStatus;
import com.restaurant.hub.enums.PaymentStatus;
import com.restaurant.hub.exception.PaymentVerificationException;
import com.restaurant.hub.exception.ResourceNotFoundException;
import com.restaurant.hub.exception.UnauthorizedException;
import com.restaurant.hub.repository.OrderRepository;
import com.restaurant.hub.repository.PaymentRepository;
import com.restaurant.hub.security.CurrentUserProvider;
import com.restaurant.hub.util.OrderStatusValidator;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CurrentUserProvider currentUserProvider;
    private final RazorpayClient razorpayClient;
    private final NotificationService notificationService;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${app.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
                           CurrentUserProvider currentUserProvider, RazorpayClient razorpayClient,
                           NotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.currentUserProvider = currentUserProvider;
        this.razorpayClient = razorpayClient;
        this.notificationService = notificationService;
    }

    @Transactional
    public CreatePaymentResponse createPaymentOrder(CreatePaymentRequest request) {
        Order order = findOwnedOrThrow(request.orderId());

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new PaymentVerificationException("This order is not awaiting payment");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentVerificationException("This order has already been paid");
        }

        long amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_" + order.getId());

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            if (payment == null) {
                payment = Payment.builder()
                        .order(order)
                        .amount(order.getTotalAmount())
                        .razorpayOrderId(razorpayOrderId)
                        .status(PaymentStatus.CREATED)
                        .build();
            } else {
                payment.setRazorpayOrderId(razorpayOrderId);
                payment.setStatus(PaymentStatus.CREATED);
                payment.setRazorpayPaymentId(null);
                payment.setRazorpaySignature(null);
            }
            paymentRepository.save(payment);

            return new CreatePaymentResponse(order.getId(), razorpayOrderId, amountInPaise, "INR", razorpayKeyId);
        } catch (RazorpayException e) {
            throw new PaymentVerificationException("Could not initiate payment: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this Razorpay order"));

        assertOwnsOrder(payment.getOrder());

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", request.razorpayOrderId());
        options.put("razorpay_payment_id", request.razorpayPaymentId());
        options.put("razorpay_signature", request.razorpaySignature());

        boolean valid;
        try {
            valid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (RazorpayException e) {
            valid = false;
        }

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            throw new PaymentVerificationException("Payment signature could not be verified");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);

        Order order = payment.getOrder();
        OrderStatusValidator.assertValidTransition(order.getStatus(), OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.CONFIRMED);
        notificationService.notifyPaymentSuccess(order);

        return PaymentResponse.from(payment);
    }

    /**
     * Handles Razorpay's server-to-server webhook. This is the source of truth
     * for payment confirmation in production, since a customer's browser can
     * close before the verify call above ever runs.
     */
    @Transactional
    public void handleWebhook(String rawPayload, String signatureHeader) {
        try {
            Utils.verifyWebhookSignature(rawPayload, signatureHeader, razorpayWebhookSecret);
        } catch (RazorpayException e) {
            throw new PaymentVerificationException("Webhook signature verification failed");
        }

        JSONObject payload = new JSONObject(rawPayload);
        String event = payload.optString("event", "");

        JSONObject payloadObj = payload.optJSONObject("payload");
        JSONObject paymentWrapper = payloadObj != null ? payloadObj.optJSONObject("payment") : null;
        JSONObject paymentEntity = paymentWrapper != null ? paymentWrapper.optJSONObject("entity") : null;

        if (paymentEntity == null) {
            return;
        }

        String razorpayOrderId = paymentEntity.optString("order_id", null);
        String razorpayPaymentId = paymentEntity.optString("id", null);
        if (razorpayOrderId == null) {
            return;
        }

        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            // Idempotent: a webhook can be delivered more than once.
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return;
            }

            if ("payment.captured".equals(event)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(razorpayPaymentId);
                Order order = payment.getOrder();
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    notificationService.notifyPaymentSuccess(order);
                }
            } else if ("payment.failed".equals(event)) {
                payment.setStatus(PaymentStatus.FAILED);
            }
        });
    }

    public PaymentResponse getByOrderId(Long orderId) {
        Order order = findOwnedOrThrow(orderId);
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this order"));
        return PaymentResponse.from(payment);
    }

    private Order findOwnedOrThrow(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));
        assertOwnsOrder(order);
        return order;
    }

    private void assertOwnsOrder(Order order) {
        User current = currentUserProvider.getCurrentUser();
        if (!order.getUser().getId().equals(current.getId())) {
            throw new UnauthorizedException("This is not your order");
        }
    }
}

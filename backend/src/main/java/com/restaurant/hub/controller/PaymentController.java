package com.restaurant.hub.controller;

import com.restaurant.hub.dto.payment.CreatePaymentRequest;
import com.restaurant.hub.dto.payment.CreatePaymentResponse;
import com.restaurant.hub.dto.payment.PaymentResponse;
import com.restaurant.hub.dto.payment.VerifyPaymentRequest;
import com.restaurant.hub.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a Razorpay order for an existing RestaurantHub order")
    public ResponseEntity<CreatePaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentOrder(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify a Razorpay payment signature after checkout completes in the browser")
    public ResponseEntity<PaymentResponse> verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay server-to-server webhook (not called by the frontend)")
    public ResponseEntity<Void> webhook(@RequestBody String rawPayload,
                                         @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(rawPayload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment status for an order")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }
}

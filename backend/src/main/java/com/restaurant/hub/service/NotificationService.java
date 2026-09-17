package com.restaurant.hub.service;

import com.restaurant.hub.entity.Order;
import com.restaurant.hub.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Extensible notification layer. Today this sends email via JavaMailSender;
 * an SMS or push channel can be added later by implementing the same
 * send(to, subject, body) shape and calling it alongside email, without
 * touching the callers below (OrderService, PaymentService, TicketService).
 *
 * Never logs anything sensitive; only event type and order/ticket id.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from}")
    private String fromAddress;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyOrderPlaced(Order order) {
        send(order, "Order placed", "Your order #" + order.getId() + " has been placed and is awaiting confirmation.");
    }

    public void notifyOrderStatusChanged(Order order) {
        send(order, "Order update", "Your order #" + order.getId() + " is now " + order.getStatus() + ".");
    }

    public void notifyPaymentSuccess(Order order) {
        send(order, "Payment received", "Payment for order #" + order.getId() + " was successful. Your order is confirmed.");
    }

    public void notifyTicketPurchased(Ticket ticket) {
        String to = ticket.getUser().getEmail();
        String subject = "Ticket confirmed: " + ticket.getEvent().getName();
        String body = "You booked " + ticket.getQuantity() + " ticket(s) for " + ticket.getEvent().getName()
                + " on " + ticket.getEvent().getEventDate() + ". Total paid: ₹" + ticket.getTotalAmount();
        dispatch(to, subject, body, "ticket:" + ticket.getId());
    }

    private void send(Order order, String subject, String body) {
        dispatch(order.getUser().getEmail(), subject, body, "order:" + order.getId());
    }

    private void dispatch(String to, String subject, String body, String eventRef) {
        log.info("Notification event={} to={}", eventRef, maskEmail(to));

        if (!mailEnabled) {
            // Local/dev default: log only, no SMTP configured yet.
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send notification email for {}: {}", eventRef, e.getMessage());
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at);
    }
}

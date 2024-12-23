package org.example.paymentsservice.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.example.paymentsservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.ticketingplatform.dtos.InitPaymentTicketResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/webhook")
public class PaymentProviderWebhook {
    private final PaymentService paymentService;
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentProviderWebhook(KafkaTemplate<String, Object> kafkaTemplate, PaymentService paymentService) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) throws IOException, SignatureVerificationException {
        String payload = getPayload(request);
        String sigHeader = request.getHeader("Stripe-Signature");

        Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    System.out.println("PaymentIntent was successful: " + event.getData().getObject().toString());
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new IllegalArgumentException("Invalid PaymentIntent data"));
                    String paymentIntentId = paymentIntent.getId();
                    paymentService.finalizePayment(paymentIntentId);                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
                    break;
            }

            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            System.err.println("Error handling Stripe webhook: " + e.getMessage());
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }

    private String getPayload(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder payload = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            payload.append(line);
        }
        return payload.toString();
    }
}

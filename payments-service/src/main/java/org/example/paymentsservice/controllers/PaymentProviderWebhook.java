package org.example.paymentsservice.controllers;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.example.paymentsservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentProviderWebhook {
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;

    public PaymentProviderWebhook(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, HttpServletRequest request) {
        String sigHeader = request.getHeader("Stripe-Signature");
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
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
}

package org.example.paymentsservice.services;

import com.example.paymentsservice.repositories.QueriesImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ticketingplatform.dtos.InitPaymentTicketRequest;
import org.ticketingplatform.dtos.InitPaymentTicketResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    @Value("${app.kafka.payments-callback-topic}")
    private String paymentsCallbackTopic;

    private final QueriesImpl queriesImpl;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PaymentService paymentService;

    public PaymentService(DataSource dataSource, KafkaTemplate<String, Object> kafkaTemplate, PaymentService paymentService) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
        this.kafkaTemplate = kafkaTemplate;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${app.kafka.payments-topic}", groupId = "default")
    public void listenFinalizePayment(InitPaymentTicketRequest initPaymentTicketRequest) {
        System.out.println("Received message: " + initPaymentTicketRequest);
        try {
            PaymentMethodCreateParams paymentMethodParams = PaymentMethodCreateParams.builder()
                    .setType(PaymentMethodCreateParams.Type.CARD)
                    .setCard(PaymentMethodCreateParams.CardDetails.builder()
                            .setNumber(initPaymentTicketRequest.cardNumber())
                            .setExpMonth(Long.valueOf(initPaymentTicketRequest.cardExpMonth()))
                            .setExpYear(Long.valueOf(initPaymentTicketRequest.cardExpYear()))
                            .setCvc(initPaymentTicketRequest.cardCvv())
                            .build())
                    .build();

            PaymentMethod paymentMethod = PaymentMethod.create(paymentMethodParams);

            PaymentIntentCreateParams paymentIntentParams = PaymentIntentCreateParams.builder()
                    .setAmount((long) (initPaymentTicketRequest.price() * 100))
                    .setCurrency("usd")
                    .setPaymentMethod(paymentMethod.getId())
                    .setConfirm(true)
                    .build();

            PaymentIntent intent = PaymentIntent.create(paymentIntentParams);

            queriesImpl.initPayment(
                    UUID.randomUUID(),
                    UUID.fromString(initPaymentTicketRequest.userId()),
                    UUID.fromString(initPaymentTicketRequest.ticketId()),
                    intent.getId(),
                    initPaymentTicketRequest.price(),
                    LocalDateTime.now(),
                    null
            );
        } catch (SQLException e) {
            System.out.println("Failed to init payment");
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    public void finalizePayment(String stripeId) throws SQLException {
        var payment = queriesImpl.finalizePayment(
                LocalDateTime.now(),
                stripeId
        );
        kafkaTemplate.send(paymentsCallbackTopic, new InitPaymentTicketResponse(
                payment.getUserId().toString(),
                payment.getTicketId().toString(),
                payment.getPrice()
        ));
    }
}

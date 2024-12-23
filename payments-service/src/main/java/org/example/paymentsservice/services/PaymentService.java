package org.example.paymentsservice.services;

import com.example.paymentsservice.repositories.QueriesImpl;
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

    public PaymentService(DataSource dataSource, KafkaTemplate<String, Object> kafkaTemplate) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.payments-topic}", groupId = "default")
    public void listenFinalizePayment(InitPaymentTicketRequest initPaymentTicketRequest) {
        System.out.println("Received message: " + initPaymentTicketRequest);
        try {
            queriesImpl.initPayment(
                    UUID.randomUUID(),
                    UUID.fromString(initPaymentTicketRequest.userId()),
                    UUID.fromString(initPaymentTicketRequest.ticketId()),
                    "stripe-id",
                    initPaymentTicketRequest.price(),
                    LocalDateTime.now(),
                    null
            );

            // move this part to callback of stripe
            queriesImpl.finalizePayment(
                    LocalDateTime.now(),
                    "stripe-id"
            );
            kafkaTemplate.send(paymentsCallbackTopic, new InitPaymentTicketResponse(
                    initPaymentTicketRequest.userId(),
                    initPaymentTicketRequest.ticketId(),
                    initPaymentTicketRequest.price()
            ));
        } catch (SQLException e) {
            System.out.println("Failed to init payment");
        }
    }
}

package org.example.eventsservice.services;

import com.example.eventsservice.repositories.QueriesImpl;
import com.example.eventsservice.repositories.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ticketingplatform.dtos.BuyTicketRequest;
import org.ticketingplatform.dtos.BuyTicketResponse;
import org.ticketingplatform.dtos.GetTicketResponse;
import org.ticketingplatform.dtos.InitPaymentTicketRequest;
import org.ticketingplatform.dtos.InitPaymentTicketResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class TicketService {
    @Value("${app.kafka.payments-topic}")
    private String paymentsTopic;

    private final QueriesImpl queriesImpl;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TicketService(DataSource dataSource, KafkaTemplate<String, Object> kafkaTemplate) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "${app.kafka.payments-callback-topic}", groupId = "default")
    public void listenFinalizePayment(InitPaymentTicketResponse initPaymentTicketResponse) {
        System.out.println("Received message: " + initPaymentTicketResponse);
        try {
            queriesImpl.updateTicket(
                    UUID.fromString(initPaymentTicketResponse.userId()),
                    UUID.fromString(initPaymentTicketResponse.ticketId()),
                    LocalDateTime.now(),
                    TicketStatus.RESERVED.toString(),
                    LocalDateTime.now()
            );
        } catch (SQLException e) {
            System.out.println("Failed to finalize payment");
        }
    }

    public void createEventTickets(UUID eventId, Integer count, Double price) throws SQLException {
        for (int i = 0; i < count; i++) {
            queriesImpl.createTicket(
                    UUID.randomUUID(),
                    null,
                    eventId,
                    null,
                    price,
                    TicketStatus.AVAILABLE.toString(),
                    LocalDateTime.now(),
                    null
            );
        }
    }

    public List<GetTicketResponse> getTicketsByEventId(UUID eventId) {
        try {
            return queriesImpl.getTickets(eventId)
                    .stream()
                    .map(ticket -> new GetTicketResponse(
                                    ticket.getId().toString(),
                                    ticket.getUserId() == null ? null : ticket.getUserId().toString(),
                                    ticket.getEventId().toString(),
                                    ticket.getPurchaseDate() == null ? null : ticket.getPurchaseDate().toString(),
                                    ticket.getPrice(),
                                    ticket.getStatus(),
                                    ticket.getCreatedAt().toString(),
                                    ticket.getUpdatedAt() == null ? null : ticket.getUpdatedAt().toString()
                            )
                    ).toList();
        } catch (SQLException e) {
            return List.of();
        }
    }

    public BuyTicketResponse reserveTicket(BuyTicketRequest buyTicketRequest)  {
        Ticket ticket;
        try {
            ticket = queriesImpl.getTicketById(UUID.fromString(buyTicketRequest.ticketId()));
            if (!ticket.getStatus().equals(TicketStatus.AVAILABLE.toString())) {
                throw new Exception("Ticket is not available");
            }

            queriesImpl.updateTicket(UUID.fromString(buyTicketRequest.userId()), ticket.getId(), null, TicketStatus.RESERVED.toString(), LocalDateTime.now());

            kafkaTemplate.send(paymentsTopic, new InitPaymentTicketRequest(
                buyTicketRequest.userId(),
                buyTicketRequest.ticketId(),
                ticket.getPrice(),
                buyTicketRequest.cardNumber(),
                buyTicketRequest.cardExpMonth(),
                buyTicketRequest.cardExpYear(),
                buyTicketRequest.cardCvv()
            ));

            return new BuyTicketResponse(
                    buyTicketRequest.userId(),
                    ticket.getEventId().toString(),
                    ticket.getId().toString(),
                    ticket.getPrice()
            );
        } catch (Exception e) {
            System.out.println("Failed to reserve a ticket");
        }
        return new BuyTicketResponse(
                null,
                null,
                null,
                null
        );
    }
}

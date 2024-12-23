package org.example.eventsservice.services;

import com.example.eventsservice.repositories.Event;
import com.example.eventsservice.repositories.QueriesImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.ticketingplatform.dtos.CreateEventRequest;
import org.ticketingplatform.dtos.CreateEventResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
public class EventService {
    @Value("${app.kafka.events-topic}")
    private String eventsTopic;

    private final QueriesImpl queriesImpl;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TicketService ticketService;

    public EventService(DataSource dataSource, KafkaTemplate<String, Object> kafkaTemplate, TicketService ticketService) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
        this.kafkaTemplate = kafkaTemplate;
        this.ticketService = ticketService;
    }

    public CreateEventResponse createEvent(CreateEventRequest createEventRequest) {
        Event event;
        try {
            event = queriesImpl.createEvent(
                    UUID.randomUUID(),
                    createEventRequest.name(),
                    createEventRequest.description(),
                    createEventRequest.location(),
                    LocalDateTime.parse(createEventRequest.startTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(createEventRequest.endTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.now()
            );

            ticketService.createEventTickets(event.getId(), createEventRequest.numberTicketsFirstClass(), createEventRequest.priceFirstClass());
            ticketService.createEventTickets(event.getId(), createEventRequest.numberTicketsSecondClass(), createEventRequest.priceSecondClass());
            ticketService.createEventTickets(event.getId(), createEventRequest.numberTicketsThirdClass(), createEventRequest.priceThirdClass());

            var response = new CreateEventResponse(
                    event.getId().toString(),
                    event.getName(),
                    event.getDescription(),
                    event.getLocation(),
                    event.getStartTime().toString(),
                    event.getEndTime().toString(),
                    event.getCreatedAt().toString()
            );
            kafkaTemplate.send(eventsTopic, response);
            return response;
        } catch (SQLException e) {
            return new CreateEventResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    public List<CreateEventResponse> getAllEvents() {
        try {
            return queriesImpl.getEvents()
                    .stream()
                    .map(event -> new CreateEventResponse(
                                    event.getId().toString(),
                                    event.getName(),
                                    event.getDescription(),
                                    event.getLocation(),
                                    event.getStartTime().toString(),
                                    event.getEndTime().toString(),
                                    event.getCreatedAt().toString()
                            )
                    ).toList();
        } catch (SQLException e) {
            return List.of();
        }
    }
}

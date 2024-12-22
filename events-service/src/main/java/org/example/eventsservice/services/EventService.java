package org.example.eventsservice.services;

import com.example.eventsservice.repositories.Event;
import com.example.eventsservice.repositories.QueriesImpl;
import org.example.eventsservice.dtos.CreateEventRequest;
import org.example.eventsservice.dtos.CreateEventResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class EventService {
    private final QueriesImpl queriesImpl;

    @Autowired
    public EventService(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
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

            for (int i = 0; i < createEventRequest.numberTicketsFirstClass(); i++) {
                queriesImpl.createTicket(
                        UUID.randomUUID(),
                        null,
                        event.getId(),
                        null,
                        createEventRequest.priceFirstClass(),
                        TicketStatus.AVAILABLE.toString(),
                        LocalDateTime.now()

                );
            }
            for (int i = 0; i < createEventRequest.numberTicketsSecondClass(); i++) {
                queriesImpl.createTicket(
                        UUID.randomUUID(),
                        null,
                        event.getId(),
                        null,
                        createEventRequest.priceSecondClass(),
                        TicketStatus.AVAILABLE.toString(),
                        LocalDateTime.now()
                );
            }
            for (int i = 0; i < createEventRequest.numberTicketsThirdClass(); i++) {
                queriesImpl.createTicket(
                        UUID.randomUUID(),
                        null,
                        event.getId(),
                        null,
                        createEventRequest.priceThirdClass(),
                        TicketStatus.AVAILABLE.toString(),
                        LocalDateTime.now()
                );
            }

            return new CreateEventResponse(
                    event.getId().toString(),
                    event.getName(),
                    event.getDescription(),
                    event.getLocation(),
                    event.getStartTime().toString(),
                    event.getEndTime().toString(),
                    event.getCreatedAt().toString()
            );
        } catch (SQLException e) {
            return new CreateEventResponse(
                   "00000000-0000-0000-0000-000000000000",
                    "ERROR",
                    "ERROR",
                    "ERROR",
                    "ERROR",
                    "ERROR",
                    "ERROR"
            );
        }
    }
}

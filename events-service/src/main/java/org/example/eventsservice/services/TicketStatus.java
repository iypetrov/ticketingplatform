package org.example.eventsservice.services;

public enum TicketStatus {
    AVAILABLE("AVAILABLE"), RESERVED("RESERVED"), SOLD("SOLD");

    private final String status;

    TicketStatus(String status) {
        this.status = status;
    }
}

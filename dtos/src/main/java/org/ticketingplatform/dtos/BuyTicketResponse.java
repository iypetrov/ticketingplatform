package org.ticketingplatform.dtos;

public record BuyTicketResponse(
        String userId,
        String eventId,
        String ticketId,
        Double price
) {
}

package org.ticketingplatform.dtos;

public record GetTicketResponse(
        String id,
        String userId,
        String eventId,
        String purchaseDate,
        Double price,
        String status,
        String createdAt,
        String updatedAt
) {
}

package org.ticketingplatform.dtos;

public record InitPaymentTicketResponse(
    String userId,
    String ticketId,
    Double price
) {
}

package org.ticketingplatform.dtos;

public record BuyTicketRequest(
        String userId,
        String ticketId,
        String cardNumber,
        String cardHolderName,
        String cardExpirationDate,
        String cardCvv
) {
}

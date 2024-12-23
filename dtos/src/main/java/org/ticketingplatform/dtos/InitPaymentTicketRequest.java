package org.ticketingplatform.dtos;

public record InitPaymentTicketRequest(
        String userId,
        String ticketId,
        Double price,
        String cardNumber,
        String cardHolderName,
        String cardExpirationDate,
        String cardCvv
) {
}

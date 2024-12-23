package org.ticketingplatform.dtos;

public record BuyTicketRequest(
        String userId,
        String ticketId,
        String cardNumber,
        Integer cardExpMonth,
        Integer cardExpYear,
        String cardCvv
) {
}

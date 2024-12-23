package org.ticketingplatform.dtos;

public record InitPaymentTicketRequest(
        String userId,
        String ticketId,
        Double price,
        String cardNumber,
        Integer cardExpMonth,
        Integer cardExpYear,
        String cardCvv
) {
}

package org.ticketingplatform.dtos;

public record CreateEventRequest(
    String name,
    String description,
    String location,
    String startTime,
    String endTime,
    Integer numberTicketsFirstClass,
    Double priceFirstClass,
    Integer numberTicketsSecondClass,
    Double priceSecondClass,
    Integer numberTicketsThirdClass,
    Double priceThirdClass
) {
}

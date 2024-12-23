package org.ticketingplatform.dtos;

public record CreateEventResponse(
        String id,
        String name,
        String description,
        String location,
        String startTime,
        String endTime,
        String createdAt
) {
}

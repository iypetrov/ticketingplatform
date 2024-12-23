package org.ticketingplatform.dtos;

public record CreateUserResponse(
    String id,
    String name,
    String email,
    String address,
    String createdAt
) {
}

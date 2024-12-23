package org.ticketingplatform.dtos;

public record CreateUserRequest(
        String name,
        String email,
        String address
) {
}

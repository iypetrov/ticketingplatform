package org.example.usersservice.services;

import com.example.usersservice.repositories.QueriesImpl;
import com.example.usersservice.repositories.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.ticketingplatform.dtos.CreateEventResponse;
import org.ticketingplatform.dtos.CreateUserRequest;
import org.ticketingplatform.dtos.CreateUserResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {
    private final QueriesImpl queriesImpl;

    public UserService(DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
    }

    @KafkaListener(topics = "${app.kafka.events-topic}", groupId = "default")
    public void listenCreateEvent(CreateEventResponse createEventResponse) {
        System.out.println("Received message: " + createEventResponse);
        List<User> users = new ArrayList<>();

        try {
            users = queriesImpl.getAllUsers();
        } catch (SQLException e) {
            System.out.println("Failed to load a user");
        }

        for (User user : users) {
            System.out.println("User " + user.getName() + " received notification for event " + createEventResponse.name());
        }
    }

    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        User user;
        try {
            user = queriesImpl.createUser(
                    UUID.randomUUID(),
                    createUserRequest.name(),
                    createUserRequest.email(),
                    createUserRequest.address(),
                    LocalDateTime.now()
            );
            return new CreateUserResponse(
                    user.getId().toString(),
                    user.getName(),
                    user.getEmail(),
                    user.getAddress(),
                    user.getCreatedAt().toString()
            );
        } catch (SQLException e) {
            return new CreateUserResponse(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    public List<CreateUserResponse> getAllUsers() {
        List<User> users;
        List<CreateUserResponse> createUserResponses = new ArrayList<>();
        try {
            users = queriesImpl.getAllUsers();
            for (User user : users) {
                createUserResponses.add(new CreateUserResponse(
                        user.getId().toString(),
                        user.getName(),
                        user.getEmail(),
                        user.getAddress(),
                        user.getCreatedAt().toString()
                ));
            }
            return createUserResponses;
        } catch (SQLException e) {
            return createUserResponses;
        }
    }
}

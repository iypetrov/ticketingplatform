package org.example.usersservice.services;

import com.example.usersservice.repositories.QueriesImpl;
import com.example.usersservice.repositories.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.ticketingplatform.dtos.CreateEventResponse;
import org.ticketingplatform.dtos.CreateUserRequest;
import org.ticketingplatform.dtos.CreateUserResponse;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {
    private final QueriesImpl queriesImpl;
    private final Environment environment;
    private final JavaMailSender mailSender;
    private final TemplateEngine htmlTemplateEngine;


    public UserService(DataSource dataSource, Environment environment, JavaMailSender mailSender, TemplateEngine htmlTemplateEngine) throws SQLException {
        Connection conn = dataSource.getConnection();
        this.queriesImpl = new QueriesImpl(conn);
        this.environment = environment;
        this.mailSender = mailSender;
        this.htmlTemplateEngine = htmlTemplateEngine;
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
            try {
                String mailFrom = environment.getProperty("spring.mail.properties.mail.smtp.from");
                String mailFromName = environment.getProperty("mail.from.name", "Identity");
                final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
                final MimeMessageHelper email;

                email = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                email.setTo(user.getEmail());
                email.setSubject("Upcoming new event");
                email.setFrom(new InternetAddress(mailFrom, mailFromName));

                final Context ctx = new Context(LocaleContextHolder.getLocale());
                ctx.setVariable("userName", user.getName());
                ctx.setVariable("eventName", createEventResponse.name());

                final String htmlContent = this.htmlTemplateEngine.process("event", ctx);
                email.setText(htmlContent, true);

                mailSender.send(mimeMessage);
                System.out.println("User " + user.getName() + " received notification for event " + createEventResponse.name());
            } catch (Exception e) {
                System.out.println("Failed to send notification to user " + user.getName());
            }
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
            System.out.println("New user is added: " + createUserRequest.email());
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

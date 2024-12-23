package org.example.usersservice.controllers;

import org.example.usersservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ticketingplatform.dtos.CreateUserRequest;
import org.ticketingplatform.dtos.CreateUserResponse;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/")
    public ResponseEntity<CreateUserResponse> createEvent(@RequestBody CreateUserRequest createUserRequest) {
        return ResponseEntity.ok().body(userService.createUser(createUserRequest));
    }

    @GetMapping("/")
    public ResponseEntity<List<CreateUserResponse>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

}

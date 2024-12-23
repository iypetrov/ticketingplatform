package org.example.eventsservice.controllers;

import org.example.eventsservice.services.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ticketingplatform.dtos.CreateEventRequest;
import org.ticketingplatform.dtos.CreateEventResponse;

import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {
    private EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/")
    public ResponseEntity<CreateEventResponse> createEvent(@RequestBody CreateEventRequest createEventRequest) {
        return ResponseEntity.ok().body(eventService.createEvent(createEventRequest));
    }

    @GetMapping("/")
    public ResponseEntity<List<CreateEventResponse>> getEvent() {
        return ResponseEntity.ok().body(
                eventService.getAllEvents()
        );
    }
}

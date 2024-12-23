package org.example.eventsservice.controllers;

import org.example.eventsservice.services.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ticketingplatform.dtos.BuyTicketRequest;
import org.ticketingplatform.dtos.BuyTicketResponse;
import org.ticketingplatform.dtos.GetTicketResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
public class TicketController {
    private TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{eventId}")
    private ResponseEntity<List<GetTicketResponse>> getTicketsByEventId(@PathVariable String eventId) {
        return ResponseEntity.ok().body(ticketService.getTicketsByEventId(UUID.fromString(eventId)));
    }

    @PutMapping("/purchase")
    private ResponseEntity<BuyTicketResponse> buyTicket(@RequestBody BuyTicketRequest buyTicketRequest) {
        return ResponseEntity.ok().body(ticketService.reserveTicket(buyTicketRequest));
    }
}

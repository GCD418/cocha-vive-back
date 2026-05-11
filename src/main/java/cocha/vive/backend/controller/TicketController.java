package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.BuyTicketRequestDTO;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.model.dto.TicketResponseDTO;
import cocha.vive.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "Ticket operations")
@PreAuthorize("hasRole('USER')")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "Get tickets for current user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        return ResponseEntity.ok(ticketService.getMyTickets());
    }

    @Operation(summary = "Buy a ticket for an event")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/buy")
    public ResponseEntity<TicketResponseDTO> buyTicket(
        @Parameter(description = "Ticket purchase request") @Valid @RequestBody BuyTicketRequestDTO request
    ) {
        return ResponseEntity.status(201).body(ticketService.createTicket(request.getEventId(), request.getQuantity()));
    }

    @Operation(summary = "Mark a ticket as used")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket marked as used"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Ticket not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Invalid state",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/use")
    @PreAuthorize("hasRole('PUBLISHER')")
    public ResponseEntity<Void> markTicketAsUsed(
        @Parameter(description = "Ticket ID to mark as used")
        @PathVariable("id") UUID id) {
        ticketService.markUsed(id);
        return ResponseEntity.noContent().build();
    }

}

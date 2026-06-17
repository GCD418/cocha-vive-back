package cocha.vive.backend.controller;

import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.EventStatus;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.model.dto.EventRejectDTO;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.model.dto.EventResponseDTO;
import cocha.vive.backend.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Event", description = "Event entity operations")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Get all public events")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of public events retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/events")
    public List<EventResponseDTO> getAllEvents(){
        return eventService.toResponseDtoList(eventService.getAllPublic());
    }

    @Operation(summary = "Get events created by the current publisher")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Publisher's events retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires PUBLISHER role")
    })
    @GetMapping("/events/my-events")
    @PreAuthorize("hasRole('PUBLISHER')")
    public List<EventResponseDTO> getMyEvents() {
        return eventService.toResponseDtoList(eventService.getMyEvents());
    }

    @Operation(summary = "Get all events (admin view, including non-public)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All events retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role")
    })
    @GetMapping("/events/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventResponseDTO> getAllEventsForAdmin() {
        return eventService.toResponseDtoList(eventService.getAllForAdmin());
    }

    @Operation(
        summary = "Create a new event",
        description = "Creates a new event with multipart form data. The 'event' part is a JSON " +
                      "object (EventRequest) and 'images' is one or more image files."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires PUBLISHER role"),
        @ApiResponse(responseCode = "404", description = "User or category not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> createEvent(
        @RequestPart("event") EventRequest dto,
        @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(dto, images));
    }

    @Operation(summary = "Get an event by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event found"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(
        @Parameter(description = "Event ID") @PathVariable Long id) {
        return ResponseEntity.ok(eventService.toResponseDto(eventService.findById(id)));
    }

    @Operation(summary = "Get upcoming active events")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully")
    })
    @GetMapping("/events/upcoming")
    public List<EventResponseDTO> getUpcomingEvents(){
        return eventService.toResponseDtoList(eventService.getUpcoming());
    }

    @Operation(summary = "Get featured (promoted) active events")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Featured events retrieved successfully")
    })
    @GetMapping("/events/featured")
    public List<EventResponseDTO> getFeaturedEvents(){
        return eventService.toResponseDtoList(eventService.getFeatured());
    }

    @Operation(summary = "Cancel an event")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/events/{id}/cancel")
    public ResponseEntity<Void> cancelEvent(
        @Parameter(description = "Event ID") @PathVariable Long id) {
        eventService.cancelEvent(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get events by category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events for the given category retrieved successfully")
    })
    @GetMapping("/events/category/{categoryId}")
    public List<EventResponseDTO> getEventsByCategory(
        @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        return eventService.toResponseDtoList(eventService.getEventsByCategoryId(categoryId));
    }

    @Operation(
        summary = "Update an event",
        description = "Updates an existing event. The 'event' part is a JSON object (EventRequest). " +
                      "The 'images' part is optional; omit it to keep existing images."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – must be event owner",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Event or category not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping(value = "/events/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PUBLISHER')")
    public ResponseEntity<Event> updateEvent(
        @Parameter(description = "Event ID") @PathVariable Long id,
        @RequestPart("event") EventRequest dto,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(eventService.update(id, dto, images));
    }

    @Operation(summary = "Approve an event (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event approved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/events/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveEvent(
        @Parameter(description = "Event ID") @PathVariable Long id) {
        eventService.updateStatus(id, EventStatus.APPROVED);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reject an event (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rejection payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Event is not in PENDING state",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/events/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectEvent(
        @Parameter(description = "Event ID") @PathVariable Long id,
        @RequestBody @Valid EventRejectDTO dto) {
        eventService.rejectEvent(id, dto);
        return ResponseEntity.noContent().build();
    }
}

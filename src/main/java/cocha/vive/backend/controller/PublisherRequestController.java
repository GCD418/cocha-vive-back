package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.dto.PublisherRequestRejectDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.service.PublisherRequestService;
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
@RequestMapping("/api/publisher-requests")
@RequiredArgsConstructor
@Tag(name = "Publisher Request", description = "Publisher access request operations")
@PreAuthorize("hasRole('ADMIN')")
public class PublisherRequestController {

    private final PublisherRequestService publisherRequestService;

    @Operation(summary = "Get all publisher requests (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role")
    })
    @GetMapping("/all")
    public List<PublisherRequestResponseDTO> getAllRequests() {
        return publisherRequestService.getAll();
    }

    @Operation(summary = "Get all pending publisher requests (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role")
    })
    @GetMapping("/pending")
    public List<PublisherRequestResponseDTO> getPendingRequests() {
        return publisherRequestService.getAllPending();
    }

    @Operation(summary = "Get a publisher request by ID (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Request not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PublisherRequestResponseDTO> getRequestById(
        @Parameter(description = "Publisher request ID") @PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.getById(id));
    }

    @Operation(
        summary = "Submit a publisher request",
        description = "Allows a regular user (ROLE_USER) to apply for publisher access. " +
                      "The 'request' part is a JSON object (PublisherRequestCreateDTO) " +
                      "and 'images' are supporting document images."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires USER role")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<PublisherRequestResponseDTO> createRequest(
        @Valid @RequestPart("request") PublisherRequestCreateDTO dto,
        @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherRequestService.createRequest(dto, images));
    }

    @Operation(summary = "Get the current user's own publisher request")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires USER role"),
        @ApiResponse(responseCode = "404", description = "No request found for this user",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/my-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PublisherRequestResponseDTO> getMyRequest() {
        return ResponseEntity.ok(publisherRequestService.getMyRequest());
    }

    @Operation(summary = "Approve a publisher request (admin)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request approved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Request not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Request is not in PENDING state",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/approve")
    public ResponseEntity<PublisherRequestResponseDTO> approveRequest(
        @Parameter(description = "Publisher request ID") @PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.approveRequest(id));
    }

    @Operation(
        summary = "Reject a publisher request (admin)",
        description = "Rejects a pending publisher request with a mandatory rejection reason."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing rejection reason",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden – requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Publisher request not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Request is not in PENDING state",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/reject")
    public ResponseEntity<PublisherRequestResponseDTO> rejectRequest(
        @Parameter(description = "Publisher request ID") @PathVariable Long id,
        @Valid @RequestBody PublisherRequestRejectDTO dto) {
        return ResponseEntity.ok(publisherRequestService.rejectRequest(id, dto));
    }
}

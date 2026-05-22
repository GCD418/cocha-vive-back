package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.BuyPromotionRequestDTO;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.model.dto.PromotionResponseDTO;
import cocha.vive.backend.service.PromotionEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Event promotion operations")
@PreAuthorize("hasRole('PUBLISHER')")
public class PromotionEventController {
    private final PromotionEventService promotionService;

    @Operation(summary = "Purchase a promotion package for an event")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Promotion created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - not the event owner",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Event is not in a promotable state",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/buy")
    public ResponseEntity<PromotionResponseDTO> buyPromotion(
        @Parameter(description = "Promotion purchase request")
        @Valid @RequestBody BuyPromotionRequestDTO request
    ) {
        return ResponseEntity.status(201).body(
            promotionService.purchasePromotion(request.getEventId(), request.getPlan()));
    }
}

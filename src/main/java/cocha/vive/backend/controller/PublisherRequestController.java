package cocha.vive.backend.controller;

import cocha.vive.backend.core.annotations.FeatureFlag;
import cocha.vive.backend.core.enums.AppFeature;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import cocha.vive.backend.service.PublisherRequestService;
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
public class PublisherRequestController {

    private final PublisherRequestService publisherRequestService;

    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PublisherRequestResponseDTO> getAllRequests() {
        return publisherRequestService.getAll();
    }

    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PublisherRequestResponseDTO> getPendingRequests() {
        return publisherRequestService.getAllPending();
    }

    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherRequestResponseDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.getById(id));
    }

    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN','USER')")
    public ResponseEntity<PublisherRequestResponseDTO> createRequest(
        @Valid @RequestPart("request") PublisherRequestCreateDTO dto,
        @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherRequestService.createRequest(dto, images));
    }
    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @GetMapping("/my-request")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PublisherRequestResponseDTO> getMyRequest() {
        return ResponseEntity.ok(publisherRequestService.getMyRequest());
    }
    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherRequestResponseDTO> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.approveRequest(id));
    }

    @FeatureFlag(AppFeature.MANAGE_PUBLISHER_REQUESTS)
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherRequestResponseDTO> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.rejectRequest(id));
    }
}

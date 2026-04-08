package cocha.vive.backend.controller;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
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
@PreAuthorize("hasAnyRole('ADMIN')")
public class PublisherRequestController {

    private final PublisherRequestService publisherRequestService;

    @GetMapping
    public List<PublisherRequest> getAllRequests() {
        return publisherRequestService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublisherRequest> createRequest(
        @Valid @RequestPart("request") PublisherRequestCreateDTO dto,
        @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherRequestService.createRequest(dto, images));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<PublisherRequest> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.approveRequest(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PublisherRequest> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(publisherRequestService.rejectRequest(id));
    }
}

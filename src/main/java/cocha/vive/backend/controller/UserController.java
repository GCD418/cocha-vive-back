package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.CompleteProfileDto;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.model.dto.UserMeDTO;
import cocha.vive.backend.model.mapper.UserMapper;
import cocha.vive.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile operations")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Complete current user's profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/complete-profile")
    public ResponseEntity<Void> completeProfile(@Valid @RequestBody CompleteProfileDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        userService.updateDocumentNumber(userEmail, dto.documentNumber(), dto.extension());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeDTO> getCurrentUser() {
        return ResponseEntity.ok(userMapper.toMeDto(userService.getActualUser()));
    }
}

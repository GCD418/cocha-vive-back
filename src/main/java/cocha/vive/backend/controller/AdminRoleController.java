package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.model.dto.RoleChangeResponseDTO;
import cocha.vive.backend.model.dto.UserMeDTO;
import cocha.vive.backend.model.mapper.UserMapper;
import cocha.vive.backend.service.AdminRoleService;
import cocha.vive.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
@Tag(name = "Admin Role Management", description = "Endpoints for promoting and demoting user roles")
public class AdminRoleController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AdminRoleService adminRoleService;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserMeDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll().stream()
            .map(userMapper::toMeDto)
            .toList());
    }

    @Operation(summary = "Promote a ROLE_USER to ROLE_ADMIN")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User promoted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role transition",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{userId}/promote")
    public ResponseEntity<RoleChangeResponseDTO> promoteToAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(adminRoleService.promoteToAdmin(userId));
    }

    @Operation(summary = "Demote a ROLE_ADMIN to ROLE_USER")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Admin demoted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role transition",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{userId}/demote")
    public ResponseEntity<RoleChangeResponseDTO> demoteToUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminRoleService.demoteToUser(userId));
    }
}

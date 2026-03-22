package cocha.vive.backend.controller;

import cocha.vive.backend.model.dto.CompleteProfileDto;
import cocha.vive.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody CompleteProfileDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        userService.updateDocumentNumber(userEmail, dto.documentNumber(), dto.extension());

        return ResponseEntity.ok().build();
    }
}

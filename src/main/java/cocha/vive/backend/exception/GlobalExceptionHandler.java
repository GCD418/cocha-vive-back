package cocha.vive.backend.exception;

import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.exception.InvalidRoleTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeatureDisabled(FeatureDisabledException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .code("RESOURCE_SHOULD_NOT_BE_FOUNDED")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidRoleTransitionException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRoleTransition(InvalidRoleTransitionException ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .code("INVALID_ROLE_TRANSITION")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .orElse("Validation error");

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .code("VALIDATION_ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .code("INTERNAL_SERVER_ERROR")
            .message("Internal server error")
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

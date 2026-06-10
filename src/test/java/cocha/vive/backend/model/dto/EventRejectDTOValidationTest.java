package cocha.vive.backend.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventRejectDTO validation tests")
class EventRejectDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("rejectionReason field")
    class RejectionReason {

        @Test
        @DisplayName("null is valid (optional)")
        void nullIsValid() {
            EventRejectDTO dto = new EventRejectDTO();
            dto.setRejectionReason(null);

            Set<ConstraintViolation<EventRejectDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("empty string is valid (optional)")
        void emptyStringIsValid() {
            EventRejectDTO dto = new EventRejectDTO();
            dto.setRejectionReason("");

            Set<ConstraintViolation<EventRejectDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("valid reason ≤ 200 characters passes")
        void reasonWithinMaxLengthPasses() {
            EventRejectDTO dto = new EventRejectDTO();
            dto.setRejectionReason("Incomplete event documentation");

            Set<ConstraintViolation<EventRejectDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("reason exactly 200 characters passes")
        void reasonAtMaxLengthPasses() {
            EventRejectDTO dto = new EventRejectDTO();
            dto.setRejectionReason("A".repeat(200));

            Set<ConstraintViolation<EventRejectDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("reason > 200 characters fails validation")
        void reasonExceedingMaxLengthFails() {
            EventRejectDTO dto = new EventRejectDTO();
            dto.setRejectionReason("A".repeat(201));

            Set<ConstraintViolation<EventRejectDTO>> violations = validator.validate(dto);

            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v ->
                v.getMessage().contains("size must be between 0 and 200"));
        }
    }
}

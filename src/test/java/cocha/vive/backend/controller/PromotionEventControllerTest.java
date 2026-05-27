package cocha.vive.backend.controller;

import cocha.vive.backend.model.PromotionPlan;
import cocha.vive.backend.model.dto.BuyPromotionRequestDTO;
import cocha.vive.backend.model.dto.PromotionResponseDTO;
import cocha.vive.backend.service.PromotionEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionController tests")
class PromotionControllerTest {

    @Mock
    private PromotionEventService promotionService;

    @InjectMocks
    private PromotionEventController promotionController;

    @Nested
    @DisplayName("POST /api/promotions/buy")
    class BuyPromotion {

        @Test
        @DisplayName("201 - crea promoción y delega al servicio")
        void shouldCreatePromotion() {
            PromotionResponseDTO responseDTO = mock(PromotionResponseDTO.class);
            when(promotionService.purchasePromotion(10L, PromotionPlan.ONE_WEEK))
                .thenReturn(responseDTO);

            BuyPromotionRequestDTO request = new BuyPromotionRequestDTO();
            request.setEventId(10L);
            request.setPlan(PromotionPlan.ONE_WEEK);

            ResponseEntity<PromotionResponseDTO> response =
                promotionController.buyPromotion(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(responseDTO);
            verify(promotionService).purchasePromotion(10L, PromotionPlan.ONE_WEEK);
        }
    }
}

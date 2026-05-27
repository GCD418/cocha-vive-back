package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.EventPromotion;
import cocha.vive.backend.model.dto.PromotionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventPromotionMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    PromotionResponseDTO toResponseDto(EventPromotion promotion);

    List<PromotionResponseDTO> toResponseDtoList(List<EventPromotion> promotions);
}

package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import cocha.vive.backend.model.dto.PublisherRequestResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = UserMapper.class)
public interface PublisherRequestMapper {

    PublisherRequest toEntity(PublisherRequestCreateDTO dto);

    PublisherRequestCreateDTO toCreateDto(PublisherRequest entity);

    @Mapping(target = "createdByUser", source = "createdByUserId")
    PublisherRequestResponseDTO toResponseDto(PublisherRequest entity);

    List<PublisherRequestResponseDTO> toResponseDtoList(List<PublisherRequest> entities);
}

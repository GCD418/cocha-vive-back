package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.PublisherRequest;
import cocha.vive.backend.model.dto.PublisherRequestCreateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PublisherRequestMapper {

    PublisherRequest toEntity(PublisherRequestCreateDTO dto);

    PublisherRequestCreateDTO toCreateDto(PublisherRequest entity);
}

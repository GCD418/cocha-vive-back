package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EventCreateDTO;
import cocha.vive.backend.model.dto.EventRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    EventCreateDTO toCreateDto(Event event);

    Event toEntity(EventCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = ".", source = "dto")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "organizedByUser", source = "organizedByUser")
    @Mapping(target = "photoLinks", source = "photoLinks")
    @Mapping(target = "eventStatus", constant = "APPROVED")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "modifiedByUserId", ignore = true)
    Event toEntity(EventRequest dto, Category category, User organizedByUser, List<String> photoLinks);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "photoLinks", ignore = true)
    void updateEventFromRequest(EventRequest dto, @MappingTarget Event event);
}

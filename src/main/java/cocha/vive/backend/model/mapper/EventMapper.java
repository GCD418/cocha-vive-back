package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.Event;
import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.EventCreateDTO;
import cocha.vive.backend.model.dto.EventRequest;
import cocha.vive.backend.model.dto.EventResponseDTO;
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
    @Mapping(target = "eventStatus", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "modifiedByUserId", ignore = true)
    Event toEntity(EventRequest dto, Category category, User organizedByUser, List<String> photoLinks);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "photoLinks", ignore = true)
    void updateEventFromRequest(EventRequest dto, @MappingTarget Event event);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "organizedByUserId", source = "organizedByUser.id")
    @Mapping(target = "organizedByUserName", expression = "java(resolveOrganizerName(event.getOrganizedByUser()))")
    @Mapping(target = "eventStatus", expression = "java(event.getEventStatus() != null ? event.getEventStatus().name() : null)")
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "promotionType", ignore = true)
    @Mapping(target = "promotionSlot", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    EventResponseDTO toResponseDto(Event event);

    default String resolveOrganizerName(User user) {
        if (user == null) {
            return null;
        }
        StringBuilder name = new StringBuilder();
        if (user.getNames() != null && !user.getNames().isBlank()) {
            name.append(user.getNames().trim());
        }
        if (user.getFirstLastName() != null && !user.getFirstLastName().isBlank()) {
            if (name.length() > 0) {
                name.append(' ');
            }
            name.append(user.getFirstLastName().trim());
        }
        if (user.getSecondLastName() != null && !user.getSecondLastName().isBlank()) {
            if (name.length() > 0) {
                name.append(' ');
            }
            name.append(user.getSecondLastName().trim());
        }
        return name.length() > 0 ? name.toString() : null;
    }
}

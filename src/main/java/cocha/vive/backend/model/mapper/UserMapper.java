package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.User;
import cocha.vive.backend.model.dto.UserCreateDTO;
import cocha.vive.backend.model.dto.UserMeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "names", source = "name")
    User toEntity(UserCreateDTO dto);

    @Mapping(target = "createdAt", source = "createdAt")
    UserMeDTO toMeDto(User user);
}

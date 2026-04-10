package cocha.vive.backend.model.mapper;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryCreateDTO dto);

    CategoryCreateDTO toCreateDto(Category category);
}

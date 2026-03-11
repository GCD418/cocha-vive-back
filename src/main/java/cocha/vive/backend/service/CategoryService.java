package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryDTO;
import cocha.vive.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public List<Category> getAll(){
        return categoryRepository.findAll();
    }

    public Category create(CategoryDTO categoryDTO){
        return categoryRepository.save(Category.builder()
            .name(categoryDTO.getName())
            .description(categoryDTO.getDescription())
            .identifyingIcon(categoryDTO.getIdentifyingIcon())
            .build());
    }

    public Category findByName(String name){
        return categoryRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("There is no category with name: " + name));
    }

    public void delete(long id, long requestingUserId){
        categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found Category"));
        categoryRepository.softDelete(id, requestingUserId);
    }

}

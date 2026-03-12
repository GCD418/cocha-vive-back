package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.model.dto.CategoryResponseDTO;
import cocha.vive.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public List<CategoryResponseDTO> getAll(){
        return categoryRepository.findAll()
            .stream()
            .map(CategoryResponseDTO::fromEntity)
            .toList();
    }

    public Category create(CategoryCreateDTO categoryCreateDTO){
        return categoryRepository.save(Category.builder()
            .name(categoryCreateDTO.getName())
            .description(categoryCreateDTO.getDescription())
            .identifyingIcon(categoryCreateDTO.getIdentifyingIcon())
            .build());
    }

    public Category findByName(String name){
        return categoryRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("There is no category with name: " + name));
    }

    @Transactional
    public void delete(Long id, Long requestingUserId){
        categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not Found Category"));
        categoryRepository.softDelete(id, requestingUserId);
    }

}

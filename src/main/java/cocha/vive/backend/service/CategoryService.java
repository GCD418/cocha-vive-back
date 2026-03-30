package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;
    public List<Category> getAll(){
        log.info("Retrieving all Categories");
        return categoryRepository.findAll();
    }

    public Category create(CategoryCreateDTO categoryCreateDTO){
        return categoryRepository.save(Category.builder()
            .name(categoryCreateDTO.getName())
            .description(categoryCreateDTO.getDescription())
            .identifyingIcon(categoryCreateDTO.getIdentifyingIcon())
            .build());
    }

    public Category findByName(String name){
        log.info("Searching for category with name: {}", name);
        return categoryRepository.findByName(name)
            .orElseThrow(() -> {
                log.warn("Not found category with name: {}", name);
                return new ResourceNotFoundException("There is no category with name: " + name);
            });
    }

    @Transactional
    public void delete(Long id){
        log.info("Soft deleting Category with id: {}", id);
        categoryRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Not found category with id: {}", id);
                return new ResourceNotFoundException("Not Found Category");
            });
        categoryRepository.softDelete(id, auditService.getActualUserId());
    }

}

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
        log.debug("Retrieving all categories");
        List<Category> categories = categoryRepository.findAll();
        log.debug("Retrieved {} categories", categories.size());
        return categories;
    }

    public Category create(CategoryCreateDTO categoryCreateDTO){
        log.info("Creating category with name: {}", categoryCreateDTO.getName());
        Category savedCategory = categoryRepository.save(Category.builder()
            .name(categoryCreateDTO.getName())
            .description(categoryCreateDTO.getDescription())
            .identifyingIcon(categoryCreateDTO.getIdentifyingIcon())
            .build());
        log.info("Category created with id: {} and name: {}", savedCategory.getId(), savedCategory.getName());
        return savedCategory;
    }

    public Category findByName(String name){
        log.debug("Searching for category with name: {}", name);
        Category category = categoryRepository.findByName(name)
            .orElseThrow(() -> {
                log.warn("Not found category with name: {}", name);
                return new ResourceNotFoundException("There is no category with name: " + name);
            });
        log.debug("Found category with id: {} and name: {}", category.getId(), category.getName());
        return category;
    }

    @Transactional
    public void delete(Long id){
        Long actualUserId = auditService.getActualUserId();
        log.info("Soft deleting category with id: {} by user id: {}", id, actualUserId);
        categoryRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Not found category with id: {}", id);
                return new ResourceNotFoundException("Not Found Category");
            });
        categoryRepository.softDelete(id, actualUserId);
        log.info("Category soft deleted with id: {}", id);
    }

}

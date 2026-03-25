package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category(), new Category()));

        List<Category> result = categoryService.getAll();

        assertEquals(2, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    void shouldCreateCategory() {
        CategoryCreateDTO dto = new CategoryCreateDTO(
            "Music",
            "Events related to music",
            "icon-url"
        );

        when(categoryRepository.save(any(Category.class)))
            .thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.create(dto);

        assertNotNull(result);
        assertEquals("Music", result.getName());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldFindCategoryByName() {
        Category category = new Category();
        category.setName("Sports");

        when(categoryRepository.findByName("Sports"))
            .thenReturn(Optional.of(category));

        Category result = categoryService.findByName("Sports");

        assertEquals("Sports", result.getName());
    }

    @Test
    void shouldThrowWhenCategoryNotFoundByName() {
        when(categoryRepository.findByName("Unknown"))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> categoryService.findByName("Unknown"));
    }

    @Test
    void shouldDeleteCategory() {
        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));
        when(auditService.getActualUserId()).thenReturn(10L);

        categoryService.delete(1L);

        verify(categoryRepository).softDelete(1L, 10L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistingCategory() {
        when(categoryRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> categoryService.delete(1L));

        verify(categoryRepository, never()).softDelete(any(), any());
    }
}

package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.model.mapper.CategoryMapper;
import cocha.vive.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .id(1L)
            .name("Music")
            .description("Musical Events")
            .identifyingIcon("Icon")
            .build();
    }

    // ─── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return all categories")
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category, category));

        List<Category> result = categoryService.getAll();

        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    // ─── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create category successfully")
    void shouldCreateCategory() {
        CategoryCreateDTO dto = new CategoryCreateDTO("Music","Desc", "Icon");
        Category mapped = Category.builder()
            .name("Music")
            .description("Desc")
            .identifyingIcon("Icon")
            .build();

        when(categoryMapper.toEntity(dto)).thenReturn(mapped);
        when(categoryRepository.save(any(Category.class)))
            .thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Music");
        assertThat(result.getDescription()).isEqualTo("Desc");

        verify(categoryMapper).toEntity(dto);
        verify(categoryRepository).save(any(Category.class));
    }

    // ─── findByName ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("should return category when exists")
        void shouldReturnCategoryWhenExists() {
            when(categoryRepository.findByName("Music")).thenReturn(Optional.of(category));

            Category result = categoryService.findByName("Music");

            assertThat(result.getName()).isEqualTo("Music");
        }

        @Test
        @DisplayName("should throw exception when not found")
        void shouldThrowWhenNotFound() {
            when(categoryRepository.findByName("X")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findByName("X"))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── delete ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete category when exists")
        void shouldDeleteCategory() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(auditService.getActualUserId()).thenReturn(99L);

            categoryService.delete(1L);

            verify(categoryRepository).softDelete(1L, 99L);
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrowWhenCategoryNotFound() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository, never()).softDelete(anyLong(), anyLong());
        }
    }
}

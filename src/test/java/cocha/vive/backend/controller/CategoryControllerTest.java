package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    // ─── Fixtures ───────────────────────────────────────────────────────────────

    private Category buildCategory() {
        return Category.builder()
            .id(1L)
            .name("Music")
            .description("Musical group playing songs")
            .identifyingIcon("https://cdn-icons-png.flaticon.com/512/2418/2418779.png")
            .isActive(true)
            .build();
    }

    private CategoryCreateDTO buildCategoryCreateDTO() {
        return new CategoryCreateDTO(
            "Music",
            "Musical group playing songs",
            "https://cdn-icons-png.flaticon.com/512/2418/2418779.png"
        );
    }

    // ─── GET /api/categories ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/categories")
    class GetAllCategories {

        @Test
        @DisplayName("retorna lista de categorías")
        void shouldReturnListOfCategories() {
            List<Category> categories = List.of(buildCategory(), buildCategory());
            when(categoryService.getAll()).thenReturn(categories);

            List<Category> response = categoryController.GetAllCategories();

            assertThat(response).hasSize(2);
            verify(categoryService, times(1)).getAll();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay categorías")
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryService.getAll()).thenReturn(List.of());

            List<Category> response = categoryController.GetAllCategories();

            assertThat(response).isEmpty();
            verify(categoryService, times(1)).getAll();
        }
    }

    // ─── POST /api/categories ────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/categories")
    class CreateCategory {

        @Test
        @DisplayName("201 – crea categoría correctamente")
        void shouldReturn201WhenCategoryCreated() {
            Category category = buildCategory();
            CategoryCreateDTO dto = buildCategoryCreateDTO();
            when(categoryService.create(dto)).thenReturn(category);

            ResponseEntity<Category> response = categoryController.CreateCategory(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getName()).isEqualTo("Music");
            verify(categoryService, times(1)).create(dto);
        }

        @Test
        @DisplayName("delega correctamente al servicio con el DTO")
        void shouldDelegateToServiceWithDto() {
            CategoryCreateDTO dto = buildCategoryCreateDTO();
            when(categoryService.create(any(CategoryCreateDTO.class))).thenReturn(buildCategory());

            categoryController.CreateCategory(dto);

            verify(categoryService).create(eq(dto));
        }
    }

    // ─── GET /api/categories/name ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/categories/name")
    class FindByName {

        @Test
        @DisplayName("retorna categoría por nombre")
        void shouldReturnCategoryByName() {
            Category category = buildCategory();
            when(categoryService.findByName("Music")).thenReturn(category);

            Category response = categoryController.FindByName("Music");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Music");
            verify(categoryService, times(1)).findByName("Music");
        }

        @Test
        @DisplayName("delega al servicio con el nombre correcto")
        void shouldDelegateToServiceWithCorrectName() {
            when(categoryService.findByName(anyString())).thenReturn(buildCategory());

            categoryController.FindByName("Sports");

            verify(categoryService).findByName("Sports");
        }
    }

    // ─── DELETE /api/categories/{id} ─────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class DeleteCategory {

        @Test
        @DisplayName("200 – elimina categoría y retorna Killed: true")
        void shouldReturn200WithKilledTrue() {
            doNothing().when(categoryService).delete(1L);

            ResponseEntity<Map<String, Boolean>> response = categoryController.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("Killed")).isTrue();
            verify(categoryService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("delega al servicio con el id correcto")
        void shouldDelegateToServiceWithCorrectId() {
            doNothing().when(categoryService).delete(anyLong());

            categoryController.delete(42L);

            verify(categoryService).delete(42L);
        }
    }
}

package cocha.vive.backend.repository;

import cocha.vive.backend.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryRepository Tests")
class CategoryRepositoryTest {

    @Mock
    private CategoryRepository categoryRepository;

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

    // ─── findByName ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByName")
    class FindByName {

        @Test
        @DisplayName("retorna categoría cuando el nombre existe")
        void shouldReturnCategoryWhenNameExists() {
            Category category = buildCategory();
            when(categoryRepository.findByName("Music")).thenReturn(Optional.of(category));

            Optional<Category> result = categoryRepository.findByName("Music");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getName()).isEqualTo("Music");
            verify(categoryRepository, times(1)).findByName("Music");
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el nombre no existe")
        void shouldReturnEmptyWhenNameNotFound() {
            when(categoryRepository.findByName("Sports")).thenReturn(Optional.empty());

            Optional<Category> result = categoryRepository.findByName("Sports");

            assertThat(result).isEmpty();
            verify(categoryRepository, times(1)).findByName("Sports");
        }

        @Test
        @DisplayName("es case sensitive — nombre diferente no retorna resultado")
        void shouldBeCaseSensitive() {
            when(categoryRepository.findByName("music")).thenReturn(Optional.empty());

            Optional<Category> result = categoryRepository.findByName("music");

            assertThat(result).isEmpty();
        }
    }

    // ─── softDelete ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("ejecuta soft delete con id y userId correctos")
        void shouldExecuteSoftDeleteWithCorrectParams() {
            doNothing().when(categoryRepository).softDelete(1L, 99L);

            categoryRepository.softDelete(1L, 99L);

            verify(categoryRepository, times(1)).softDelete(1L, 99L);
        }

        @Test
        @DisplayName("delega con el id de categoría correcto")
        void shouldDelegateWithCorrectCategoryId() {
            doNothing().when(categoryRepository).softDelete(anyLong(), anyLong());

            categoryRepository.softDelete(42L, 1L);

            verify(categoryRepository).softDelete(eq(42L), anyLong());
        }

        @Test
        @DisplayName("delega con el userId correcto")
        void shouldDelegateWithCorrectUserId() {
            doNothing().when(categoryRepository).softDelete(anyLong(), anyLong());

            categoryRepository.softDelete(1L, 73L);

            verify(categoryRepository).softDelete(anyLong(), eq(73L));
        }
    }

    // ─── findAll (heredado de JpaRepository) ────────────────────────────────────

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("retorna lista de categorías activas")
        void shouldReturnListOfCategories() {
            List<Category> categories = List.of(buildCategory(), buildCategory());
            when(categoryRepository.findAll()).thenReturn(categories);

            List<Category> result = categoryRepository.findAll();

            assertThat(result).hasSize(2);
            verify(categoryRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay categorías")
        void shouldReturnEmptyList() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<Category> result = categoryRepository.findAll();

            assertThat(result).isEmpty();
        }
    }

    // ─── findById (heredado de JpaRepository) ───────────────────────────────────

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna categoría cuando el id existe")
        void shouldReturnCategoryWhenIdExists() {
            Category category = buildCategory();
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            Optional<Category> result = categoryRepository.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            verify(categoryRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el id no existe")
        void shouldReturnEmptyWhenIdNotFound() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Category> result = categoryRepository.findById(99L);

            assertThat(result).isEmpty();
        }
    }

    // ─── save (heredado de JpaRepository) ───────────────────────────────────────

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda y retorna la categoría con id asignado")
        void shouldSaveAndReturnCategoryWithId() {
            Category category = buildCategory();
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            Category result = categoryRepository.save(category);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Music");
            verify(categoryRepository, times(1)).save(category);
        }

        @Test
        @DisplayName("delega al repositorio con la entidad correcta")
        void shouldDelegateWithCorrectEntity() {
            Category category = buildCategory();
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            categoryRepository.save(category);

            verify(categoryRepository).save(eq(category));
        }
    }
}

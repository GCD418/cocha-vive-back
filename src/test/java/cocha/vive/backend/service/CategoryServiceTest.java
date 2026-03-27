package cocha.vive.backend.service;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .id(1L)
            .name("Music")
            .description("Musical Events")
            .identifyingIcon("A nice Icon")
            .build();
    }

    @Nested
    @DisplayName("findByName()")
    class FindByName {
        @Test
        @DisplayName("Returns the category if it exists")
        void getById_returnsCategory_whenExists() {
            when(categoryRepository.findByName("Music")).thenReturn(Optional.of(category));

            Category result = categoryService.findByName("Music");
            assertThat(result.getName()).isEqualTo("Music");
        }

        @Test
        @DisplayName("Throws an exception when category doesn't exist")
        void throwsException_whenNotFound(){
            when(categoryRepository.findByName("Verstappen")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findByName("Verstappen"))
                .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

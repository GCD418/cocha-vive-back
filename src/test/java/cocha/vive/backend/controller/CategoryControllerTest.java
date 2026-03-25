package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category;
    private CategoryCreateDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .id(1L)
            .name("Music")
            .description("Musical events")
            .identifyingIcon("icon.png")
            .isActive(true)
            .build();

        categoryDTO = new CategoryCreateDTO("Music", "Musical events", "icon.png");
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        List<Category> categories = Arrays.asList(category);
        given(categoryService.getAll()).willReturn(categories);

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("Music"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Simula usuario Admin para el PreAuthorize
    void createCategory_ShouldReturnCreated() throws Exception {
        given(categoryService.create(any(CategoryCreateDTO.class))).willReturn(category);

        mockMvc.perform(post("/api/categories")
                .with(csrf()) // Necesario si tienes CSRF habilitado
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Music"));
    }

    @Test
    void findByName_ShouldReturnCategory() throws Exception {
        given(categoryService.findByName("Music")).willReturn(category);

        mockMvc.perform(get("/api/categories/name")
                .param("name", "Music"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Music"));
    }

    @Test
    void deleteCategory_ShouldReturnSuccessMap() throws Exception {
        // Al ser void no necesitamos 'given', solo verificar que no explote
        mockMvc.perform(delete("/api/categories/{id}", 1L)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.Killed").value(true));

        Mockito.verify(categoryService).delete(1L);
    }
}

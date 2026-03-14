package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.model.dto.ErrorResponseDTO;
import cocha.vive.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category entity operations")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Get all active categories")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/categories")
    public List<Category> GetAllCategories(){
        return categoryService.getAll();
    }

    @Operation(summary = "Create a new category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/categories")
    public ResponseEntity<Category> CreateCategory(@Valid @RequestBody CategoryCreateDTO category){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(category));
    }

    @Operation(summary = "Get a Category by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/categories/name")
    public Category FindByName(@Parameter(description = "Name of the category to retrieve") @RequestParam(name = "name") String name){
        return categoryService.findByName(name);
    }

    @Operation(summary = "Soft Delete a Category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category deleted successfully",
            content = @Content(schema = @Schema(implementation = DeleteCategoryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Category not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/categories/{id}/{userId}")
    public ResponseEntity<Map<String, Boolean>> delete(@Parameter(description = "ID of the category to delete") @PathVariable("id") Long id,
                                                       @Parameter(description = "ID of the user making the operation") @PathVariable("userId") Long userId) {
        categoryService.delete(id, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Killed", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    @Schema(name = "DeleteCategoryResponse", description = "Delete operation outcome")
    private static class DeleteCategoryResponse {
        @Schema(description = "True when the category was soft-deleted", example = "true", name = "Killed")
        public Boolean killed;
    }
}

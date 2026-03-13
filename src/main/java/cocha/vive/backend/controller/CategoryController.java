package cocha.vive.backend.controller;

import cocha.vive.backend.exception.ResourceNotFoundException;
import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @Tag(name = "Category", description = "Category entire entity operations")
    @Operation(summary = "Get all active categories")
    @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
    @GetMapping("/categories")
    public List<Category> GetAllCategories(){
        return categoryService.getAll();
    }

    @Tag(name = "Category Create", description = "Category entity without administrative parameters")
    @Operation(summary = "Create a new category")
    @ApiResponse(responseCode = "200", description = "Category created successfully")
    @PostMapping("/categories")
    public Category CreateCategory(@RequestBody CategoryCreateDTO category){
        return categoryService.create(category);
    }

    @Tag(name = "Category", description = "Category entire entity operations")
    @Operation(summary = "Get a Category by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found",
        content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class)))
    })
    @GetMapping("/categories/name")
    public Category FindByName(@Parameter(description = "Name of the category to retrieve") @RequestParam(name = "name") String name){
        return categoryService.findByName(name);
    }

    @Tag(name = "Category", description = "Category entire entity operations")
    @Operation(summary = "Soft Delete a Category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/categories/{id}/{userId}")
    public ResponseEntity<Map<String, Boolean>> delete(@Parameter(description = "ID of the category to delete") @PathVariable("id") Long id,
                                                       @Parameter(description = "ID of the user making the operation") @PathVariable("userId") Long userId) {
        categoryService.delete(id, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Killed", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}

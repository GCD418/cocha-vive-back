package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.model.dto.CategoryCreateDTO;
import cocha.vive.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Category", description = "Category basic CRUD operations")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Get all active categories")
    @ApiResponse(responseCode = "200", description = "List of categories retrieved successfully")
    @GetMapping("/categories")
    public List<Category> GetAllCategories(){
        return categoryService.getAll();
    }

    @Operation(summary = "Create a new category")
    @ApiResponse(responseCode = "200", description = "Category created successfully")
    @PostMapping("/categories")
    public Category CreateCategory(@RequestBody CategoryCreateDTO category){
        return categoryService.create(category);
    }

    @Operation(summary = "Get a Category by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/categories/name")
    public Category FindByName(@Parameter(description = "Name of the category to retrieve") @RequestParam(name = "name") String name){
        return categoryService.findByName(name);
    }

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

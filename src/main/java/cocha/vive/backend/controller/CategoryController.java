package cocha.vive.backend.controller;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.service.CategoryService;
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

    @GetMapping("/categories")
    public List<Category> GetAllCategories(){
        return categoryService.getAll();
    }

    @PostMapping("/categories")
    public Category CreateCategory(@RequestBody Category category){
        return categoryService.create(category);
    }

    @GetMapping("/categories/name")
    public Category FindByName(@RequestBody String name){
        return categoryService.findByName(name);
    }

    @DeleteMapping("/employees/{id}/{userId}")
    public ResponseEntity<Map<String, Boolean>> delete(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        categoryService.delete(id, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Killed", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}

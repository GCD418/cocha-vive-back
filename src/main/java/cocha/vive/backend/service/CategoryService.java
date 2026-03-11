package cocha.vive.backend.service;

import cocha.vive.backend.model.Category;
import cocha.vive.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    public List<Category> getAll(){
        return categoryRepository.findByIsActiveTrue();
    }

    public Category createCategory(@RequestBody Category category){
        return categoryRepository.save(category);
    }
}

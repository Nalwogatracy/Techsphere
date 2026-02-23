
package com.app.TechSphere.service;

import com.app.TechSphere.model.Category;
import com.app.TechSphere.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryRepository categoryRepo;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,CategoryRepository categoryRepo) {
        this.categoryRepository = categoryRepository;
        this.categoryRepo = categoryRepo;
    }

    // Fetch all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Fetch single category by slug
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug).orElse(null);
    }
    public Category save(Category category) {
        return categoryRepo.save(category);
    }

    public Category findById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    public Category update(Category category) {
        return categoryRepository.save(category);
    }
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.delete(category);
    }

    public long getCategoryCount() {
        return categoryRepository.count();
    }

}


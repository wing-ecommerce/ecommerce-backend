package com.example.ecommerce_backend.service.Impl;

import com.example.ecommerce_backend.dto.request.CategoryRequest;
import com.example.ecommerce_backend.dto.response.CategoryResponse;
import com.example.ecommerce_backend.entity.Category;
import com.example.ecommerce_backend.repository.CategoryRepository;
import com.example.ecommerce_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // GET ALL CATEGORIES (without pagination)
    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    // GET ALL CATEGORIES WITH PAGINATION
    @Override
    public Page<CategoryResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return categoryRepository.findAll(pageable)
                .map(CategoryResponse::fromEntity);
    }

    // GET CATEGORY BY ID
    @Override
    public CategoryResponse getById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryResponse.fromEntity(category);
    }

    // CREATE CATEGORY
    @Override
    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .slug(request.getSlug())
                .build();
        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    // UPDATE CATEGORY
    @Override
    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    // DELETE CATEGORY
    @Override
    public void delete(String id) {
        categoryRepository.deleteById(id);
    }

}

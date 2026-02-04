package com.example.ecommerce_backend.service;
import com.example.ecommerce_backend.dto.request.CategoryRequest;
import com.example.ecommerce_backend.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAll();

    CategoryResponse getById(String id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(String id, CategoryRequest request);

    void delete(String id);
    Page<CategoryResponse> getAll(int page, int size);
}


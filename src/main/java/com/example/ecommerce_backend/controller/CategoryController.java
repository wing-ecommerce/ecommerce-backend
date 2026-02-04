package com.example.ecommerce_backend.controller;


import com.example.ecommerce_backend.dto.request.CategoryRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.CategoryResponse;
import com.example.ecommerce_backend.service.CategoryService;
//import com.example.ecommerce_backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

//     CREATE CATEGORY
    @PostMapping
    public ApiResponse<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request
    ) {
        return ApiResponse.success(categoryService.create(request));
    }

    //    GET ALL CATEGORIES or GET BY PAGE & SIZE
    @GetMapping
    public ApiResponse<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            return ApiResponse.success(categoryService.getAll(page, size));
        } else {
            return ApiResponse.success(categoryService.getAll());
        }
    }
    // GET CATEGORY BY ID
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable String id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    // UPDATE CATEGORY
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    // DELETE CATEGORY
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ApiResponse.success("Category deleted successfully");
    }

}

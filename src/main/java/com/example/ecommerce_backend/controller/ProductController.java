package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.ProductRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.ProductResponse;
import com.example.ecommerce_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // CREATE PRODUCT (ADMIN only)
    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    // UPDATE PRODUCT (ADMIN only)
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    // DELETE PRODUCT (ADMIN only)
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success("Product deleted successfully");
    }

    // GET PRODUCT BY ID (Public)
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    // GET ALL PRODUCTS (Public + optional pagination)
    @GetMapping
    public ApiResponse<?> getAll(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Page<ProductResponse> paged = productService.getAll(page, size);
            return ApiResponse.success(paged);
        } else {
            List<ProductResponse> list = productService.getAll();
            return ApiResponse.success(list);
        }
    }
    @GetMapping("/category/{categoryId}")
    public ApiResponse<Page<ProductResponse>> getProductsByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(productService.getProductsByCategory(categoryId, page, size));
    }

    // FILTER PRODUCTS BY CATEGORY (See all)
    @GetMapping("/category/{categoryId}/all")
    public ApiResponse<List<ProductResponse>> getAllProductsByCategory(@PathVariable String categoryId) {
        return ApiResponse.success(productService.getAllProductsByCategory(categoryId));
    }
}

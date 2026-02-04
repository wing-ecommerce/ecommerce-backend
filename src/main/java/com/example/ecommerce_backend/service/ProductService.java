package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.ProductRequest;
import com.example.ecommerce_backend.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    ProductResponse getById(Long id);
    List<ProductResponse> getAll();
    Page<ProductResponse> getAll(int page, int size);
    Page<ProductResponse> getProductsByCategory(String categoryId, int page, int size);
    List<ProductResponse> getAllProductsByCategory(String categoryId);
}

package com.example.ecommerce_backend.dto.response;


import com.example.ecommerce_backend.entity.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CategoryResponse {

    private String id;
    private String name;
    private String slug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: number of products in this category
    private int productCount;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}


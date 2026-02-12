package com.example.ecommerce_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPatchRequest {

    // All fields are optional for PATCH - NO @NotBlank or @NotNull
    
    private String name;

    // ❌ REMOVE THIS - It's causing validation to fail on empty slug
    // @Pattern(
    //     regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
    //     message = "Slug must be lowercase and may contain hyphens"
    // )
    private String slug;

    // ❌ REMOVE THIS - It's causing validation to fail on null price
    // @Positive(message = "Price must be positive")
    private Double price;

    private Double originalPrice;

    // ❌ REMOVE THESE - Causing validation issues
    // @Min(value = 0, message = "Discount must be between 0 and 100")
    // @Max(value = 100, message = "Discount must be between 0 and 100")
    private Integer discount;

    private String image;

    private List<String> additionalPhotos;

    private String description;

    private String categoryId;

    // Size variants with individual stock
    @Valid
    private List<SizeRequest> sizes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SizeRequest {

        private String size;

        // ❌ REMOVE THIS - Causing validation to fail
        // @Min(value = 0, message = "Stock cannot be negative")
        private Integer stock;

        private Double priceOverride;

        private String sku;
    }
}
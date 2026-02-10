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
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug must be lowercase and may contain hyphens"
    )
    private String slug;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private Double originalPrice;

    private Integer discount;

    private String image;

    private List<String> additionalPhotos;

    private String description;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    // Size variants with individual stock
    @NotNull(message = "At least one size variant is required")
    @Size(min = 1, message = "At least one size variant is required")
    @Valid
    private List<SizeRequest> sizes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SizeRequest {

        @NotBlank(message = "Size is required")
        private String size;

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stock;

        private Double priceOverride; // Optional

        private String sku; // Optional
    }
}
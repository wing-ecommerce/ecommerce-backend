package com.example.ecommerce_backend.dto.request;

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
            regexp = "^[a-z]+$",
            message = "Value must contain lowercase letters only"
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
    @Min(value = 1, message = "Stock must be at least 1")
    private Integer stock;
    private List<String> sizes;
    @NotBlank(message = "Category ID is required")
    private String categoryId;
}

package com.example.ecommerce_backend.dto.response;

import com.example.ecommerce_backend.entity.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private Double price;
    private Double originalPrice;
    private Integer discount;
    private String image;
    private List<String> additionalPhotos;
    private String description;
    private Boolean inStock;
    private List<String> sizes;
    private String categoryId;
    private String categoryName;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .discount(product.getDiscount())
                .image(product.getImage())
                .additionalPhotos(product.getAdditionalPhotos())
                .description(product.getDescription())
                .inStock(product.getInStock())
                .sizes(product.getSizes())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }
}

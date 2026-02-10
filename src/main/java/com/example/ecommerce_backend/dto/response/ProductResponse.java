package com.example.ecommerce_backend.dto.response;

import com.example.ecommerce_backend.entity.Product;
import com.example.ecommerce_backend.entity.ProductSize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

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
    private Integer stock; // Total stock across all sizes
    private List<SizeResponse> sizes; // Individual size variants
    private String categoryId;
    private String categoryName;

    @Getter
    @Setter
    @Builder
    public static class SizeResponse {
        private Long id;
        private String size;
        private Integer stock;
        private Double priceOverride;
        private String sku;
        private Double effectivePrice; // Either priceOverride or product price
    }

    public static ProductResponse fromEntity(Product product) {
        List<SizeResponse> sizeResponses = product.getSizes().stream()
                .map(size -> SizeResponse.builder()
                        .id(size.getId())
                        .size(size.getSize())
                        .stock(size.getStock())
                        .priceOverride(size.getPriceOverride())
                        .sku(size.getSku())
                        .effectivePrice(size.getPriceOverride() != null ? 
                                size.getPriceOverride() : product.getPrice())
                        .build())
                .collect(Collectors.toList());

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
                .stock(product.getTotalStock()) // Total stock
                .sizes(sizeResponses)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }
}
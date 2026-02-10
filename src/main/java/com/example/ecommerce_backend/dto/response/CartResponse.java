package com.example.ecommerce_backend.dto.response;

import com.example.ecommerce_backend.entity.Cart;
import com.example.ecommerce_backend.entity.CartItem;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Integer uniqueItems;
    private Double subtotal;
    private Double tax;
    private Double total;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSlug;
        private String productImage;
        private Long sizeId;
        private String sizeName;
        private Integer quantity;
        private Double price;
        private Double total;
        private Integer availableStock;
    }

    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSlug(item.getProduct().getSlug())
                        .productImage(item.getProduct().getImage())
                        .sizeId(item.getProductSize().getId())
                        .sizeName(item.getProductSize().getSize())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .total(item.getTotal())
                        .availableStock(item.getProductSize().getStock())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .uniqueItems(cart.getUniqueItems())
                .subtotal(cart.getSubtotal())
                .tax(cart.getTax())
                .total(cart.getTotal())
                .build();
    }
}
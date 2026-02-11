package com.example.ecommerce_backend.dto.response;

import com.example.ecommerce_backend.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long userId;
    private Long addressId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> items;
    private Integer totalItems;
    private Double subtotal;
    private Double shipping;
    private Double tax;
    private Double total;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDelivery;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
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
    }

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
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
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .items(itemResponses)
                .totalItems(order.getTotalItems())
                .subtotal(order.getSubtotal())
                .shipping(order.getShipping())
                .tax(order.getTax())
                .total(order.getTotal())
                .notes(order.getNotes())
                .estimatedDelivery(order.getEstimatedDelivery())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
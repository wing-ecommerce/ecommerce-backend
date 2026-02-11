package com.example.ecommerce_backend.dto.request;

import com.example.ecommerce_backend.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Subtotal is required")
    @Min(value = 0, message = "Subtotal must be at least 0")
    private Double subtotal;

    @NotNull(message = "Shipping cost is required")
    @Min(value = 0, message = "Shipping must be at least 0")
    private Double shipping;

    @NotNull(message = "Tax is required")
    @Min(value = 0, message = "Tax must be at least 0")
    private Double tax;

    @NotNull(message = "Total is required")
    @Min(value = 0, message = "Total must be at least 0")
    private Double total;

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Product size ID is required")
        private Long productSizeId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be at least 0")
        private Double price;
    }
}
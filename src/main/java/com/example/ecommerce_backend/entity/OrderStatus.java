package com.example.ecommerce_backend.entity;

public enum OrderStatus {
    PENDING,      // Order placed, awaiting confirmation
    CONFIRMED,    // Order confirmed by admin
    PROCESSING,   // Order being prepared
    SHIPPED,      // Order shipped
    DELIVERED,    // Order delivered
    CANCELLED,    // Order cancelled
    RETURNED      // Order returned
}
package com.example.ecommerce_backend.entity;

public enum PaymentStatus {
    PENDING,      // Payment not yet received
    PAID,         // Payment received
    FAILED,       // Payment failed
    REFUNDED      // Payment refunded
}
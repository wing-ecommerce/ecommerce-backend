package com.example.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "product_sizes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "size"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String size; // S, M, L, XL, etc.

    @Column(nullable = false)
    private Integer stock;

    private Double priceOverride; // Optional: different price for this size

    private String sku; // Optional: unique SKU for this variant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
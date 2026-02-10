package com.example.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private Double price;

    private Double originalPrice;

    private Integer discount;

    private String image;

    /* Additional photos */
    @ElementCollection
    @CollectionTable(
            name = "product_additional_photos",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "photo")
    private List<String> additionalPhotos = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    /* Category */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /* Variants (sizes with individual stock) */
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductSize> sizes = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper methods for managing sizes
    public void addSize(ProductSize size) {
        sizes.add(size);
        size.setProduct(this);
    }

    public void removeSize(ProductSize size) {
        sizes.remove(size);
        size.setProduct(null);
    }

    public void clearSizes() {
        sizes.forEach(size -> size.setProduct(null));
        sizes.clear();
    }

    // Calculate total stock across all sizes
    public Integer getTotalStock() {
        return sizes.stream()
                .mapToInt(ProductSize::getStock)
                .sum();
    }
}
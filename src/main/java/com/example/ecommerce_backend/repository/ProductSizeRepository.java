package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.Product;
import com.example.ecommerce_backend.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    
    List<ProductSize> findByProduct(Product product);
    
    Optional<ProductSize> findByProductAndSize(Product product, String size);
    
    void deleteByProduct(Product product);
}
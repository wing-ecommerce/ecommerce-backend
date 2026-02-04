package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.Category;
import com.example.ecommerce_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);
    Page<Product> findByCategory(Category category, Pageable pageable);
    List<Product> findByCategory(Category category);
}

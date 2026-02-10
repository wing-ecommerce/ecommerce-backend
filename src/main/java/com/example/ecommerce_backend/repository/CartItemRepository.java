package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.Cart;
import com.example.ecommerce_backend.entity.CartItem;
import com.example.ecommerce_backend.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByCartAndProductSize(Cart cart, ProductSize productSize);
    
    void deleteByCart(Cart cart);
}
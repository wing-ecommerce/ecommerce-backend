package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.AddToCartRequest;
import com.example.ecommerce_backend.dto.request.UpdateCartItemRequest;
import com.example.ecommerce_backend.dto.response.CartResponse;

public interface CartService {
    
    CartResponse getCart(Long userId);
    
    CartResponse addToCart(Long userId, AddToCartRequest request);
    
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);
    
    CartResponse removeCartItem(Long userId, Long cartItemId);
    
    void clearCart(Long userId);
}
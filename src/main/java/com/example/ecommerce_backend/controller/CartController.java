package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.AddToCartRequest;
import com.example.ecommerce_backend.dto.request.UpdateCartItemRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.CartResponse;
import com.example.ecommerce_backend.service.CartService;
import com.example.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    // GET CART
    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        Long userId = getUserId();
        return ApiResponse.success(cartService.getCart(userId));
    }

    // ADD TO CART
    @PostMapping
    public ApiResponse<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getUserId();
        return ApiResponse.success(cartService.addToCart(userId, request));
    }

    // UPDATE CART ITEM
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = getUserId();
        return ApiResponse.success(cartService.updateCartItem(userId, cartItemId, request));
    }

    // REMOVE CART ITEM
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> removeCartItem(@PathVariable Long cartItemId) {
        Long userId = getUserId();
        return ApiResponse.success(cartService.removeCartItem(userId, cartItemId));
    }

    // CLEAR CART
    @DeleteMapping
    public ApiResponse<String> clearCart() {
        Long userId = getUserId();
        cartService.clearCart(userId);
        return ApiResponse.success("Cart cleared successfully");
    }

    /**
     * Get userId from authenticated user
     * Uses existing UserService.getCurrentUser() method
     */
    private Long getUserId() {
        return userService.getCurrentUser().getId();
    }
}
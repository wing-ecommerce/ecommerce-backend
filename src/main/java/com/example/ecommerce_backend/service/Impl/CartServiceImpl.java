package com.example.ecommerce_backend.service.Impl;

import com.example.ecommerce_backend.dto.request.AddToCartRequest;
import com.example.ecommerce_backend.dto.request.UpdateCartItemRequest;
import com.example.ecommerce_backend.dto.response.CartResponse;
import com.example.ecommerce_backend.entity.Cart;
import com.example.ecommerce_backend.entity.CartItem;
import com.example.ecommerce_backend.entity.Product;
import com.example.ecommerce_backend.entity.ProductSize;
import com.example.ecommerce_backend.exception.CartLimitExceededException;
import com.example.ecommerce_backend.exception.InsufficientStockException;
import com.example.ecommerce_backend.exception.ResourceNotFoundException;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.repository.CartRepository;
import com.example.ecommerce_backend.repository.ProductRepository;
import com.example.ecommerce_backend.repository.ProductSizeRepository;
import com.example.ecommerce_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    // Cart limits configuration
    private static final int MAX_CART_ITEMS = 50;
    private static final int MAX_UNIQUE_ITEMS = 20;
    private static final int MAX_QUANTITY_PER_ITEM = 10;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        return CartResponse.fromEntity(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Validate product size exists
        ProductSize productSize = productSizeRepository.findById(request.getProductSizeId())
                .orElseThrow(() -> new ResourceNotFoundException("Product size not found"));

        // Verify product size belongs to product
        if (!productSize.getProduct().getId().equals(product.getId())) {
            throw new BadRequestException("Product size does not belong to this product");
        }

        // Check if size is in stock
        if (productSize.getStock() <= 0) {
            throw new InsufficientStockException("Size " + productSize.getSize() + " is out of stock");
        }

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductSize().getId().equals(productSize.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Validate max quantity per item
            if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new CartLimitExceededException("Maximum " + MAX_QUANTITY_PER_ITEM + 
                    " items allowed per product size. You currently have " + 
                    existingItem.getQuantity() + " in your cart.");
            }

            // Validate stock availability
            if (newQuantity > productSize.getStock()) {
                throw new InsufficientStockException("Only " + productSize.getStock() + 
                    " items available in stock for size " + productSize.getSize() + 
                    ". You currently have " + existingItem.getQuantity() + " in your cart.");
            }

            // Validate total cart items
            int totalItemsAfterUpdate = cart.getTotalItems() - existingItem.getQuantity() + newQuantity;
            if (totalItemsAfterUpdate > MAX_CART_ITEMS) {
                throw new CartLimitExceededException("Maximum " + MAX_CART_ITEMS + " total items allowed in cart.");
            }

            existingItem.setQuantity(newQuantity);
        } else {
            // Add new item
            
            // Validate unique items limit
            if (cart.getUniqueItems() >= MAX_UNIQUE_ITEMS) {
                throw new CartLimitExceededException("Maximum " + MAX_UNIQUE_ITEMS + " different items allowed in cart.");
            }

            // Validate quantity
            int quantity = request.getQuantity();
            if (quantity > MAX_QUANTITY_PER_ITEM) {
                throw new CartLimitExceededException("Maximum " + MAX_QUANTITY_PER_ITEM + 
                    " items allowed per product size.");
            }

            if (quantity > productSize.getStock()) {
                throw new InsufficientStockException("Only " + productSize.getStock() + 
                    " items available in stock for size " + productSize.getSize() + ".");
            }

            // Validate total cart items
            int totalItemsAfterAdd = cart.getTotalItems() + quantity;
            if (totalItemsAfterAdd > MAX_CART_ITEMS) {
                throw new CartLimitExceededException("Maximum " + MAX_CART_ITEMS + " total items allowed in cart.");
            }

            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .productSize(productSize)
                    .quantity(quantity)
                    .price(productSize.getPriceOverride() != null ? 
                        productSize.getPriceOverride() : product.getPrice())
                    .build();

            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        int newQuantity = request.getQuantity();

        // Validate quantity
        if (newQuantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        // Validate max quantity per item
        if (newQuantity > MAX_QUANTITY_PER_ITEM) {
            throw new CartLimitExceededException("Maximum " + MAX_QUANTITY_PER_ITEM + 
                " items allowed per product size.");
        }

        // Validate stock availability
        if (newQuantity > cartItem.getProductSize().getStock()) {
            throw new InsufficientStockException("Only " + cartItem.getProductSize().getStock() + 
                " items available in stock for size " + cartItem.getProductSize().getSize() + ".");
        }

        // Validate total cart items
        int otherItemsQuantity = cart.getItems().stream()
                .filter(item -> !item.getId().equals(cartItemId))
                .mapToInt(CartItem::getQuantity)
                .sum();

        if (otherItemsQuantity + newQuantity > MAX_CART_ITEMS) {
            throw new CartLimitExceededException("Maximum " + MAX_CART_ITEMS + " total items allowed in cart.");
        }

        cartItem.setQuantity(newQuantity);
        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.clearItems();
        cartRepository.save(cart);
    }

    private Cart createNewCart(Long userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .build();
        return cartRepository.save(cart);
    }
}
package com.example.ecommerce_backend.service.Impl;

import com.example.ecommerce_backend.dto.request.CreateOrderRequest;
import com.example.ecommerce_backend.dto.response.OrderResponse;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.entity.*;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.exception.InsufficientStockException;
import com.example.ecommerce_backend.exception.ResourceNotFoundException;
import com.example.ecommerce_backend.repository.OrderRepository;
import com.example.ecommerce_backend.repository.ProductRepository;
import com.example.ecommerce_backend.repository.ProductSizeRepository;
import com.example.ecommerce_backend.service.CartService;
import com.example.ecommerce_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        // Validate order total
        double calculatedTotal = request.getSubtotal() + request.getShipping() + request.getTax();
        if (Math.abs(calculatedTotal - request.getTotal()) > 0.01) {
            throw new BadRequestException("Order total mismatch");
        }

        // Create order
        Order order = Order.builder()
                .userId(userId)
                .addressId(request.getAddressId())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .subtotal(request.getSubtotal())
                .shipping(request.getShipping())
                .tax(request.getTax())
                .total(request.getTotal())
                .notes(request.getNotes())
                .estimatedDelivery(LocalDateTime.now().plusDays(3)) // 3 days delivery
                .build();

        // Add items and reduce stock
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            // Validate product
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Validate product size
            ProductSize productSize = productSizeRepository.findById(itemRequest.getProductSizeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product size not found"));

            // Verify product size belongs to product
            if (!productSize.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException("Product size does not belong to this product");
            }

            // Check stock availability
            if (productSize.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + product.getName() + 
                        " (Size: " + productSize.getSize() + "). " +
                        "Available: " + productSize.getStock() + 
                        ", Requested: " + itemRequest.getQuantity()
                );
            }

            // Reduce stock
            productSize.setStock(productSize.getStock() - itemRequest.getQuantity());
            productSizeRepository.save(productSize);

            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productSize(productSize)
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear user's cart
        try {
            cartService.clearCart(userId);
            log.info("Cart cleared for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to clear cart for user {}: {}", userId, e.getMessage());
        }

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order belongs to user (unless admin)
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to view this order");
        }

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to view this order");
        }

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        return mapToPageResponse(orderPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAll(pageable);

        return mapToPageResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);

        // Update payment status if order is delivered
        if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to: {}", order.getOrderNumber(), status);

        return OrderResponse.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this order");
        }

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order cannot be cancelled");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            ProductSize productSize = item.getProductSize();
            productSize.setStock(productSize.getStock() + item.getQuantity());
            productSizeRepository.save(productSize);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled: {}", order.getOrderNumber());
        return OrderResponse.fromEntity(cancelledOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        orderRepository.delete(order);
        log.info("Order deleted: {}", order.getOrderNumber());
    }

    private PageResponse<OrderResponse> mapToPageResponse(Page<Order> orderPage) {
        return PageResponse.<OrderResponse>builder()
                .content(orderPage.getContent().stream()
                        .map(OrderResponse::fromEntity)
                        .toList())
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();
    }
}
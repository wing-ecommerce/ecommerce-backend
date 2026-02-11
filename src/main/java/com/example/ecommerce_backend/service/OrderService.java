package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.CreateOrderRequest;
import com.example.ecommerce_backend.dto.response.OrderResponse;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.entity.OrderStatus;

public interface OrderService {
    
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    
    OrderResponse getOrderById(Long orderId, Long userId);
    
    OrderResponse getOrderByOrderNumber(String orderNumber, Long userId);
    
    PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size);
    
    PageResponse<OrderResponse> getAllOrders(int page, int size);
    
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    
    OrderResponse cancelOrder(Long orderId, Long userId);
    
    void deleteOrder(Long orderId);
}
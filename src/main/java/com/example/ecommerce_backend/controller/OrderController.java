package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.CreateOrderRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.OrderResponse;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.entity.OrderStatus;
import com.example.ecommerce_backend.service.OrderService;
import com.example.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /**
     * CREATE ORDER
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        Long userId = getUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    /**
     * GET ORDER BY ID
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        Long userId = getUserId();
        OrderResponse order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    /**
     * GET ORDER BY ORDER NUMBER
     * GET /api/v1/orders/number/{orderNumber}
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(
            @PathVariable String orderNumber) {
        Long userId = getUserId();
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber, userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    /**
     * GET USER'S ORDERS
     * GET /api/v1/orders/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId();
        PageResponse<OrderResponse> orders = orderService.getUserOrders(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    /**
     * GET ALL ORDERS (ADMIN ONLY)
     * GET /api/v1/orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderResponse> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    /**
     * UPDATE ORDER STATUS (ADMIN ONLY)
     * PATCH /api/v1/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        OrderResponse order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated successfully"));
    }

    /**
     * CANCEL ORDER
     * POST /api/v1/orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        Long userId = getUserId();
        OrderResponse order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled successfully"));
    }

    /**
     * DELETE ORDER (ADMIN ONLY)
     * DELETE /api/v1/orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully"));
    }

    /**
     * Get userId from authenticated user
     */
    private Long getUserId() {
        return userService.getCurrentUser().getId();
    }
}
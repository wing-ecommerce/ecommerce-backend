package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.Order;
import com.example.ecommerce_backend.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    List<Order> findByUserId(Long userId);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND " +
           "o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findUserOrdersBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findAllOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    boolean existsByOrderNumber(String orderNumber);
    
    Long countByUserId(Long userId);
}
package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.dto.response.UserResponse;
import com.example.ecommerce_backend.entity.Role;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.ResourceNotFoundException;
import com.example.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller
 * Handles administrative operations for user management
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserRepository userRepository;
    
    /**
     * Enable a user account
     */
    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(@PathVariable @NonNull Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setEnabled(true);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success(
                mapToUserResponse(user), 
                "User enabled successfully"
        ));
    }
    
    /**
     * Disable a user account
     */
    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(@PathVariable @NonNull Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setEnabled(false);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success(
                mapToUserResponse(user), 
                "User disabled successfully"
        ));
    }
    
    /**
     * Change a user's role
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable @NonNull Long id,
            @RequestParam Role role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setRole(role);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success(
                mapToUserResponse(user), 
                "User role updated successfully"
        ));
    }
    
    /**
     * Get users by role with pagination
     */
    @GetMapping("/users/role/{role}")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByRole(
            @PathVariable Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable)
                .map(user -> user.getRole().equals(role) ? user : null);
        
        PageResponse<UserResponse> response = PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream()
                        .filter(user -> user != null && user.getRole().equals(role))
                        .map(this::mapToUserResponse)
                        .toList())
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
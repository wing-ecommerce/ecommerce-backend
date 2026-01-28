package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.ChangePasswordRequest;
import com.example.ecommerce_backend.dto.request.UpdateUserRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.dto.response.UserResponse;
import com.example.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Current user retrieved successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUser().id == #id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PageResponse<UserResponse> users = userService.getAllUsers(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<UserResponse> users = userService.searchUsers(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(users, "Search completed successfully"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }
    
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
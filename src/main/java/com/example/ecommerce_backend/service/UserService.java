package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.ChangePasswordRequest;
import com.example.ecommerce_backend.dto.request.UpdateUserRequest;
import com.example.ecommerce_backend.dto.response.PageResponse;
import com.example.ecommerce_backend.dto.response.UserResponse;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.exception.DuplicateResourceException;
import com.example.ecommerce_backend.exception.ResourceNotFoundException;
import com.example.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse getCurrentUser() {
        User user = getCurrentAuthenticatedUser();
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToUserResponse(user);
    }
    
    public PageResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<User> userPage = userRepository.findAll(pageable);
        
        return mapToPageResponse(userPage);
    }
    
    public PageResponse<UserResponse> searchUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.searchUsers(searchTerm, pageable);
        
        return mapToPageResponse(userPage);
    }
    
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check authorization
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getId().equals(id) && !currentUser.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("You are not authorized to update this user");
        }
        
        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        
        // Update other fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentAuthenticatedUser();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Verify new password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check authorization
        User currentUser = getCurrentAuthenticatedUser();
        if (!currentUser.getId().equals(id) && !currentUser.getRole().name().equals("ADMIN")) {
            throw new BadRequestException("You are not authorized to delete this user");
        }
        
        userRepository.delete(user);
    }
    
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
    
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
    
    private PageResponse<UserResponse> mapToPageResponse(Page<User> userPage) {
        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream()
                        .map(this::mapToUserResponse)
                        .toList())
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
    }
}
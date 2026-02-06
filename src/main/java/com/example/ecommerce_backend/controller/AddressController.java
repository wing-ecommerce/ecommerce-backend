package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.AddressRequest;
import com.example.ecommerce_backend.dto.response.AddressResponse;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    
    /**
     * Get all addresses for the current authenticated user
     * 
     * @return List of user's addresses (default address first)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getCurrentUserAddresses() {
        List<AddressResponse> addresses = addressService.getCurrentUserAddresses();
        return ResponseEntity.ok(
                ApiResponse.success(addresses, "Addresses retrieved successfully")
        );
    }
    
    /**
     * Get all addresses for a specific user (Admin only)
     * 
     * @param userId User ID
     * @return List of user's addresses
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @PathVariable Long userId
    ) {
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(
                ApiResponse.success(addresses, "User addresses retrieved successfully")
        );
    }
    
    /**
     * Get a specific address by ID
     * 
     * @param id Address ID
     * @return Address details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(
            @PathVariable Long id
    ) {
        AddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(
                ApiResponse.success(address, "Address retrieved successfully")
        );
    }
    
    /**
     * Get default address for current user
     * 
     * @return Default address
     */
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress() {
        AddressResponse address = addressService.getDefaultAddress();
        return ResponseEntity.ok(
                ApiResponse.success(address, "Default address retrieved successfully")
        );
    }
    
    /**
     * Create a new address for the current user
     * 
     * @param request Address data
     * @return Created address
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse address = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(address, "Address created successfully")
        );
    }
    
    /**
     * Update an existing address
     * 
     * @param id Address ID
     * @param request Updated address data
     * @return Updated address
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse address = addressService.updateAddress(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(address, "Address updated successfully")
        );
    }
    
    /**
     * Set an address as default
     * 
     * @param id Address ID
     * @return Updated address
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long id
    ) {
        AddressResponse address = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(
                ApiResponse.success(address, "Default address set successfully")
        );
    }
    
    /**
     * Delete an address
     * 
     * @param id Address ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id
    ) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Address deleted successfully")
        );
    }
    
    /**
     * Delete all addresses for a user (Admin only)
     * 
     * @param userId User ID
     * @return Success message
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllUserAddresses(
            @PathVariable Long userId
    ) {
        addressService.deleteAllUserAddresses(userId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "All user addresses deleted successfully")
        );
    }
}
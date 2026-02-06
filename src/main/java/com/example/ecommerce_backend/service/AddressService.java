package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.AddressRequest;
import com.example.ecommerce_backend.dto.response.AddressResponse;
import com.example.ecommerce_backend.entity.Address;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.exception.ResourceNotFoundException;
import com.example.ecommerce_backend.repository.AddressRepository;
import com.example.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all addresses for the current authenticated user
     */
    public List<AddressResponse> getCurrentUserAddresses() {
        User user = getCurrentAuthenticatedUser();
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all addresses for a specific user (Admin only)
     */
    public List<AddressResponse> getUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return addresses.stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific address by ID
     */
    public AddressResponse getAddressById(Long id) {
        User user = getCurrentAuthenticatedUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        return mapToAddressResponse(address);
    }
    
    /**
     * Get default address for current user
     */
    public AddressResponse getDefaultAddress() {
        User user = getCurrentAuthenticatedUser();
        Address address = addressRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
        
        return mapToAddressResponse(address);
    }
    
    /**
     * Create a new address for the current user
     */
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        User user = getCurrentAuthenticatedUser();
        
        // Check if this is the first address - make it default automatically
        boolean isFirstAddress = !addressRepository.existsByUserId(user.getId());
        boolean shouldBeDefault = isFirstAddress || (request.getIsDefault() != null && request.getIsDefault());
        
        // If setting as default, unset other default addresses
        if (shouldBeDefault) {
            setAllAddressesNonDefault(user.getId());
        }
        
        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .isDefault(shouldBeDefault)
                .build();
        
        Address savedAddress = addressRepository.save(address);
        log.info("Created new address for user: {}", user.getUsername());
        
        return mapToAddressResponse(savedAddress);
    }
    
    /**
     * Update an existing address
     */
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) {
        User user = getCurrentAuthenticatedUser();
        
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        // Update fields
        address.setFullName(request.getFullName());
        address.setEmail(request.getEmail());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());

        // Handle default flag
        if (request.getIsDefault() != null && request.getIsDefault() && !address.getIsDefault()) {
            setAllAddressesNonDefault(user.getId());
            address.setIsDefault(true);
        }
        
        Address updatedAddress = addressRepository.save(address);
        log.info("Updated address {} for user: {}", id, user.getUsername());
        
        return mapToAddressResponse(updatedAddress);
    }
    
    /**
     * Set an address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long id) {
        User user = getCurrentAuthenticatedUser();
        
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        // Set all other addresses to non-default
        addressRepository.setOtherAddressesNonDefault(user.getId(), id);
        
        // Set this address as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Set address {} as default for user: {}", id, user.getUsername());
        
        return mapToAddressResponse(updatedAddress);
    }
    
    /**
     * Delete an address
     */
    @Transactional
    public void deleteAddress(Long id) {
        User user = getCurrentAuthenticatedUser();
        
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        boolean wasDefault = address.getIsDefault();
        
        addressRepository.delete(address);
        log.info("Deleted address {} for user: {}", id, user.getUsername());
        
        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Set address {} as new default for user: {}", newDefault.getId(), user.getUsername());
            }
        }
    }
    
    /**
     * Delete all addresses for a user (Admin only or when user deletes account)
     */
    @Transactional
    public void deleteAllUserAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        addressRepository.deleteByUserId(userId);
        log.info("Deleted all addresses for user id: {}", userId);
    }
    
    /**
     * Helper method to set all addresses to non-default for a user
     */
    private void setAllAddressesNonDefault(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        addresses.forEach(addr -> addr.setIsDefault(false));
        addressRepository.saveAll(addresses);
    }
    
    /**
     * Get current authenticated user
     */
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
    
    /**
     * Map Address entity to AddressResponse DTO
     */
    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .fullName(address.getFullName())
                .email(address.getEmail())
                .phone(address.getPhone())
                .address(address.getAddress())
                .city(address.getCity())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
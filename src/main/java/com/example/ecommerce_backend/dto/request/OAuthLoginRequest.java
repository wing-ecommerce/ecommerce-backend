package com.example.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginRequest {
    
    @NotBlank(message = "OAuth provider is required")
    private String provider; 
    
    @NotBlank(message = "OAuth token is required")
    private String token; 
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    private String profileImageUrl;
    
    @NotBlank(message = "OAuth provider ID is required")
    private String providerId; 
}
package com.example.ecommerce_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @Builder.Default
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("user")
    private UserResponse user;
}
package com.example.ecommerce_backend.controller;

import com.example.ecommerce_backend.dto.request.LoginRequest;
import com.example.ecommerce_backend.dto.request.RegisterRequest;
import com.example.ecommerce_backend.dto.response.ApiResponse;
import com.example.ecommerce_backend.dto.response.AuthenticationResponse;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthenticationResponse response = authenticationService.register(
                request, 
                httpRequest, 
                httpResponse
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthenticationResponse response = authenticationService.login(
                request, 
                httpRequest, 
                httpResponse
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthenticationResponse response = authenticationService.refreshToken(
                httpRequest, 
                httpResponse
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logout(httpRequest, httpResponse);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @AuthenticationPrincipal User user,
            HttpServletResponse httpResponse
    ) {
        authenticationService.logoutAllDevices(user, httpResponse);
        
        return ResponseEntity.ok(ApiResponse.success(
                null, 
                "Logged out from all devices successfully"
        ));
    }
}
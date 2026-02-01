package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.LoginRequest;
import com.example.ecommerce_backend.dto.request.RegisterRequest;
import com.example.ecommerce_backend.dto.response.AuthenticationResponse;
import com.example.ecommerce_backend.dto.response.UserResponse;
import com.example.ecommerce_backend.entity.Role;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.exception.DuplicateResourceException;
import com.example.ecommerce_backend.repository.UserRepository;
import com.example.ecommerce_backend.security.JwtService;
import com.example.ecommerce_backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    
    @Transactional
    public AuthenticationResponse register(
            @NonNull RegisterRequest request,
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());
        
        return generateAuthenticationResponse(user, httpRequest, httpResponse);
    }
    @Transactional
    public AuthenticationResponse login(
            @NonNull LoginRequest request,
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        // Find user
        User user = userRepository.findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail()
                )
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("User logged in: {}", user.getUsername());
        
        return generateAuthenticationResponse(user, httpRequest, httpResponse);
    }
    
    @Transactional
    public AuthenticationResponse refreshToken(
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        // Get RAW refresh token from cookie
        String rawRefreshToken = cookieUtil.getRefreshTokenFromCookie(httpRequest)
                .orElseThrow(() -> new BadRequestException("Refresh token not found"));
        
        // Rotate token: verify old token, create new one, revoke old one
        // Returns new RAW token
        String newRawToken = refreshTokenService.rotateRefreshToken(rawRefreshToken, httpRequest);
        
        // Verify the new token to get user information
        // This verifies by hashing and looking up in DB
        User user = refreshTokenService.getUserFromToken(newRawToken);
        
        // Generate new access token
        String accessToken = jwtService.generateToken(user);
        
        // Set new RAW refresh token in cookie
        cookieUtil.createRefreshTokenCookie(httpResponse, newRawToken);
        
        log.info("Tokens refreshed for user: {}", user.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(user))
                .build();
    }
    
   @Transactional
    public void logout(
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        // Get RAW refresh token from cookie
        cookieUtil.getRefreshTokenFromCookie(httpRequest).ifPresent(rawToken -> {
            // Revoke refresh token (by hashing and finding in DB)
            refreshTokenService.revokeRefreshToken(rawToken);
            log.info("User logged out, token revoked");
        });
        
        // Delete cookie
        cookieUtil.deleteRefreshTokenCookie(httpResponse);
    }
    
   @Transactional
    public void logoutAllDevices(
            @NonNull User user,
            @NonNull HttpServletResponse httpResponse
    ) {
        refreshTokenService.revokeAllUserTokens(user);
        cookieUtil.deleteRefreshTokenCookie(httpResponse);
        log.info("User logged out from all devices: {}", user.getUsername());
    }
    
    private AuthenticationResponse generateAuthenticationResponse(
            @NonNull User user,
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        // Generate access token (short-lived, stored in memory on frontend)
        String accessToken = jwtService.generateToken(user);
        
        // Generate RAW refresh token
        // Service hashes it and stores hash in database
        // Returns the RAW token (only time it exists unhashed)
        String rawRefreshToken = refreshTokenService.createRefreshToken(user, httpRequest);
        
        // Set RAW refresh token in HTTP-only cookie (client never sees the hash)
        cookieUtil.createRefreshTokenCookie(httpResponse, rawRefreshToken);
        
        // Return only access token in response (refresh token in cookie)
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(@NonNull User user) {
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
package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.dto.request.LoginRequest;
import com.example.ecommerce_backend.dto.request.OAuthLoginRequest;
import com.example.ecommerce_backend.dto.request.RegisterRequest;
import com.example.ecommerce_backend.dto.response.AuthenticationResponse;
import com.example.ecommerce_backend.dto.response.UserResponse;
import com.example.ecommerce_backend.entity.OAuthProvider;
import com.example.ecommerce_backend.entity.Role;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.exception.DuplicateResourceException;
import com.example.ecommerce_backend.repository.UserRepository;
import com.example.ecommerce_backend.security.JwtService;
import com.example.ecommerce_backend.util.CookieUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
    private final GoogleTokenValidatorService googleTokenValidator;
    
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
                .oauthProvider(OAuthProvider.LOCAL)  // NEW: Mark as local auth
                .emailVerified(false)  // NEW: Email not verified for local registration
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
    
    /**
     * OAuth Login - Handles Google, Facebook, GitHub, etc.
     * Creates new user if doesn't exist, updates existing user if exists.
     */
    @Transactional
    public AuthenticationResponse oauthLogin(
            @NonNull OAuthLoginRequest request,
            @NonNull HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse
    ) {
        log.info("OAuth login attempt with provider: {}", request.getProvider());
        
        // Validate OAuth provider
        OAuthProvider provider;
        try {
            provider = OAuthProvider.valueOf(request.getProvider().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid OAuth provider: " + request.getProvider());
        }
        
        // Validate that it's not LOCAL provider (should use regular login)
        if (provider == OAuthProvider.LOCAL) {
            throw new BadRequestException("Cannot use LOCAL provider for OAuth login");
        }
        
        // Validate token based on provider
        if (provider == OAuthProvider.GOOGLE) {
            validateGoogleToken(request);
        }
        // TODO: Add other providers (Facebook, GitHub) here in the future
        
        // Check if user exists with this OAuth provider and provider ID
        User user = userRepository
                .findByOauthProviderAndOauthProviderId(provider, request.getProviderId())
                .orElse(null);
        
        if (user == null) {
            // User doesn't exist with this OAuth account
            // Check if email exists with different auth method
            user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user != null) {
                // Email exists but with different auth method
                handleExistingEmailConflict(user, provider);
            } else {
                // Email doesn't exist - create new user
                log.info("Creating new OAuth user: {}", request.getEmail());
                user = createOAuthUser(request, provider);
            }
        } else {
            // User exists with this OAuth account - update their info
            log.info("Updating existing OAuth user: {}", request.getEmail());
            user = updateOAuthUser(user, request);
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("OAuth login successful for user: {} (provider: {})", user.getEmail(), provider);
        
        return generateAuthenticationResponse(user, httpRequest, httpResponse);
    }
    
    /**
     * Validates Google ID token with Google's servers
     */
    private void validateGoogleToken(OAuthLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenValidator.validateToken(request.getToken());
        
        if (payload == null) {
            throw new BadRequestException("Invalid Google token");
        }
        
        // Verify the payload data matches request
        if (!payload.getSubject().equals(request.getProviderId())) {
            throw new BadRequestException("Token provider ID mismatch");
        }
        
        if (!payload.getEmail().equals(request.getEmail())) {
            throw new BadRequestException("Token email mismatch");
        }
        
        log.info("Google token validated successfully for: {}", request.getEmail());
    }
    
    /**
     * Handles case where email exists but with different auth method
     */
    private void handleExistingEmailConflict(User existingUser, OAuthProvider newProvider) {
        if (existingUser.getOauthProvider() != null && 
            existingUser.getOauthProvider() != newProvider) {
            // Email exists with different OAuth provider
            throw new DuplicateResourceException(
                    String.format("Email already registered with %s", 
                            existingUser.getOauthProvider())
            );
        } else if (existingUser.getOauthProvider() == OAuthProvider.LOCAL) {
            // Email exists with local password authentication
            throw new DuplicateResourceException(
                    "Email already registered. Please login with your password"
            );
        }
    }
    
    /**
     * Creates a new user from OAuth data
     */
    private User createOAuthUser(OAuthLoginRequest request, OAuthProvider provider) {
        // Generate unique username from email
        String baseUsername = request.getEmail().split("@")[0];
        String username = generateUniqueUsername(baseUsername);
        
        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(null) // OAuth users don't have password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .profileImageUrl(request.getProfileImageUrl())
                .role(Role.USER) // Default role for new users
                .oauthProvider(provider)
                .oauthProviderId(request.getProviderId())
                .emailVerified(true) // OAuth providers verify emails
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created new OAuth user with username: {}", username);
        
        return savedUser;
    }
    
    /**
     * Updates existing OAuth user with latest data from provider
     */
    private User updateOAuthUser(User user, OAuthLoginRequest request) {
        // Update user info from OAuth provider (in case it changed)
        boolean updated = false;
        
        if (!request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            updated = true;
        }
        
        if (!request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            updated = true;
        }
        
        if (request.getProfileImageUrl() != null && 
            !request.getProfileImageUrl().equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(request.getProfileImageUrl());
            updated = true;
        }
        
        if (!user.getEmailVerified()) {
            user.setEmailVerified(true);
            updated = true;
        }
        
        if (updated) {
            userRepository.save(user);
            log.info("Updated OAuth user info: {}", user.getEmail());
        }
        
        return user;
    }
    
    /**
     * Generates a unique username by appending numbers if needed
     */
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername.replaceAll("[^a-zA-Z0-9]", ""); // Remove special chars
        
        if (username.isEmpty()) {
            username = "user";
        }
        
        // Check if base username is available
        if (!userRepository.existsByUsername(username)) {
            return username;
        }
        
        // Append numbers until we find an available username
        int counter = 1;
        String candidateUsername;
        do {
            candidateUsername = username + counter;
            counter++;
        } while (userRepository.existsByUsername(candidateUsername));
        
        return candidateUsername;
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
    
    /**
     * Generates authentication response with access token and refresh token cookie
     */
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
    
    private UserResponse mapToUserResponse(@NonNull User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())  
                .role(user.getRole())
                .oauthProvider(user.getOauthProvider())  
                .emailVerified(user.getEmailVerified())  
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
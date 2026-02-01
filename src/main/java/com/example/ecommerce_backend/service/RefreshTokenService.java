package com.example.ecommerce_backend.service;

import com.example.ecommerce_backend.entity.RefreshToken;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.exception.BadRequestException;
import com.example.ecommerce_backend.repository.RefreshTokenRepository;
import com.example.ecommerce_backend.util.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    
    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshTokenExpiration;
    
    @Value("${application.security.token.max-active-tokens:5}")
    private int maxActiveTokens;
    
    @Value("${application.security.token.rotation.enabled:true}")
    private boolean tokenRotationEnabled;
    
    @Transactional
    @NonNull
    public String createRefreshToken(@NonNull User user, @NonNull HttpServletRequest request) {
        // Generate cryptographically secure random token
        String rawToken = tokenHashUtil.generateSecureToken();
        
        // Hash the token before storing
        String tokenHash = tokenHashUtil.hashToken(rawToken);
        
        // Check active token limit
        enforceTokenLimit(user);
         
        // Create refresh token entity (with hash, not raw token)
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();
        
        refreshTokenRepository.save(refreshToken); 
        // Return the RAW token (only time it exists unhashed)
        return rawToken;
    }
    
    @NonNull
    public RefreshToken verifyRefreshToken(@NonNull String rawToken) {
        // Hash the provided token
        String tokenHash = tokenHashUtil.hashToken(rawToken);
        
        // Find token by hash
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Attempted to use invalid/unknown refresh token");
                    return new BadRequestException("Invalid refresh token");
                });
        
        if (refreshToken.isRevoked()) {
            log.warn("Attempted to use revoked refresh token for user: {}", 
                    refreshToken.getUser().getUsername());
            throw new BadRequestException("Refresh token has been revoked");
        }
        
        if (refreshToken.isExpired()) {
            log.warn("Attempted to use expired refresh token for user: {}", 
                    refreshToken.getUser().getUsername());
            throw new BadRequestException("Refresh token has expired");
        }
        
        return refreshToken;
    }
    
    @NonNull
    public User getUserFromToken(@NonNull String rawToken) {
        RefreshToken refreshToken = verifyRefreshToken(rawToken);
        return refreshToken.getUser();
    }
    
    @Transactional
    @NonNull
    public String rotateRefreshToken(@NonNull String oldRawToken, @NonNull HttpServletRequest request) {
        if (!tokenRotationEnabled) {
            // If rotation disabled, just verify and return same token
            verifyRefreshToken(oldRawToken);
            return oldRawToken;
        }
        
        // Verify old token
        RefreshToken oldRefreshToken = verifyRefreshToken(oldRawToken);
        
        // Create new refresh token
        String newRawToken = createRefreshToken(oldRefreshToken.getUser(), request);
        String newTokenHash = tokenHashUtil.hashToken(newRawToken);
        
        // Mark old token as replaced
        oldRefreshToken.setReplacedByTokenHash(newTokenHash);
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);
        
        log.info("Rotated refresh token for user: {}", oldRefreshToken.getUser().getUsername());
        
        return newRawToken;
    }
    
    @Transactional
    public void revokeRefreshToken(@NonNull String rawToken) {
        String tokenHash = tokenHashUtil.hashToken(rawToken);
        
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(refreshToken -> {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
        });
    }
    
    @Transactional
    public void revokeAllUserTokens(@NonNull User user) {
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
        log.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }
    
    @NonNull
    public List<RefreshToken> getActiveUserTokens(@NonNull User user) {
        return refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
    }
    
    private void enforceTokenLimit(@NonNull User user) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUser(user, LocalDateTime.now());
        
        if (activeTokenCount >= maxActiveTokens) {
            // Revoke oldest tokens
            List<RefreshToken> tokens = refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
            tokens.stream()
                    .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .limit(activeTokenCount - maxActiveTokens + 1)
                    .forEach(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                        log.info("Auto-revoked old token for user: {} (exceeded limit)", 
                                user.getUsername());
                    });
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        
        // Also cleanup old revoked tokens (older than 30 days)
        refreshTokenRepository.deleteRevokedTokensBefore(LocalDateTime.now().minusDays(30));
        
        log.info("Completed cleanup of expired refresh tokens");
    }

}
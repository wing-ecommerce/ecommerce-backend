package com.example.ecommerce_backend.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for secure token hashing
 * Uses SHA-256 for one-way hashing of refresh tokens
 */
@Component
public class TokenHashUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int RANDOM_TOKEN_LENGTH = 64; // bytes
    
    /**
     * Generate a cryptographically secure random token
     * 
     * @return Base64-encoded random token (512 bits)
     */
    public String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[RANDOM_TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Hash a token using SHA-256
     * 
     * @param token The token to hash
     * @return SHA-256 hash of the token (hex string)
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Verify if a token matches a hash
     * 
     * @param token The token to verify
     * @param hash The hash to compare against
     * @return true if token matches hash
     */
    public boolean verifyToken(String token, String hash) {
        String tokenHash = hashToken(token);
        return constantTimeEquals(tokenHash, hash);
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     * 
     * @param a First string
     * @param b Second string
     * @return true if strings are equal
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        
        return result == 0;
    }
    
    /**
     * Convert byte array to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
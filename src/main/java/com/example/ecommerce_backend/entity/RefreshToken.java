package com.example.ecommerce_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_token_hash", columnList = "token_hash"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * SHA-256 hash of the refresh token
     * The actual token is NEVER stored in the database
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "replaced_by_token_hash", length = 64)
    private String replacedByTokenHash;
    
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(revoked);
    }
    
    public boolean isRevoked() {
        return Boolean.TRUE.equals(revoked);
    }
    
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}
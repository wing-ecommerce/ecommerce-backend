package com.example.ecommerce_backend.repository;

import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.entity.OAuthProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByOauthProviderAndOauthProviderId(OAuthProvider provider, String providerId);

    Optional<User> findByEmailAndOauthProvider(String email, OAuthProvider provider);
       
    Boolean existsByOauthProviderAndOauthProviderId(OAuthProvider provider, String providerId);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    // Full-text search using LIKE (can be replaced with PostgreSQL full-text search)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // PostgreSQL full-text search (requires tsvector column)
    @Query(value = "SELECT * FROM users WHERE " +
           "to_tsvector('english', username || ' ' || email || ' ' || " +
           "COALESCE(first_name, '') || ' ' || COALESCE(last_name, '')) @@ " +
           "plainto_tsquery('english', :searchTerm)",
           nativeQuery = true)
    Page<User> searchUsersFullText(@Param("searchTerm") String searchTerm, Pageable pageable);
}
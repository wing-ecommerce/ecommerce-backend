package com.example.ecommerce_backend.config;

import com.example.ecommerce_backend.entity.Role;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        createDefaultAdminUser();
    }
    
    private void createDefaultAdminUser() {
        String adminUsername = "admin";
        String adminEmail = "admin@ecommerce.com";
        
        // Check if admin already exists
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user already exists. Skipping creation.");
            return;
        }
        
        // Create admin user
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin123!@#"))
                .firstName("Admin")
                .lastName("User")
                .phoneNumber("+1234567890")
                .role(Role.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        userRepository.save(admin);
        
        log.info("=".repeat(60));
        log.info("DEFAULT ADMIN ACCOUNT CREATED");
        log.info("=".repeat(60));
        log.info("Username: {}", adminUsername);
        log.info("Email: {}", adminEmail);
        log.info("Password: Admin123!@#");
        log.info("Role: ADMIN");
        log.info("=".repeat(60));
        log.info("IMPORTANT: Change the admin password after first login!");
        log.info("=".repeat(60));
    }
}
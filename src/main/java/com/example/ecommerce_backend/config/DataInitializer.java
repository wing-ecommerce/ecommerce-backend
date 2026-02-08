package com.example.ecommerce_backend.config;

import com.example.ecommerce_backend.entity.Role;
import com.example.ecommerce_backend.entity.User;
import com.example.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.create:false}")
    private boolean createAdmin;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (!createAdmin) {
            log.info("Admin auto-creation is disabled.");
            return;
        }

        createDefaultAdminUser();
    }

    private void createDefaultAdminUser() {

        if (userRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin user already exists. Skipping creation.");
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(admin);

        log.info("================================================");
        log.info("DEFAULT ADMIN ACCOUNT CREATED");
        log.info("Username: {}", adminUsername);
        log.info("Email: {}", adminEmail);
        log.info("Role: ADMIN");
        log.warn("IMPORTANT: Change admin password after first login!");
        log.info("================================================");
    }
}
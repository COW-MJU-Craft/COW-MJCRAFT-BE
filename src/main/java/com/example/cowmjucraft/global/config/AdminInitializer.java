package com.example.cowmjucraft.global.config;

import com.example.cowmjucraft.domain.auth.domain.entity.Role;
import com.example.cowmjucraft.domain.auth.domain.entity.User;
import com.example.cowmjucraft.domain.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a default admin account on application startup (local profile only).
 * Assumes the password property is already BCrypt-hashed.
 */
@RequiredArgsConstructor
@Component
public class AdminInitializer {

    private final UserRepository userRepository;

    @Value("${admin.user-id}")
    private String adminUserId;

    @Value("${admin.password}")
    private String adminPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        seedAdminIfNecessary();
    }

    @Transactional
    public void seedAdminIfNecessary() {
        userRepository.findByUserId(adminUserId)
                .ifPresentOrElse(
                        ignored -> {},
                        () -> userRepository.save(
                                new User(
                                        adminUserId,
                                        "Admin",
                                        adminUserId + "@example.com",
                                        adminPassword,
                                        Role.ADMIN
                                )
                        )
                );
    }
}

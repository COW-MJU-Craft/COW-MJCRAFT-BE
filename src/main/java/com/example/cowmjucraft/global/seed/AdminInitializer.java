package com.example.cowmjucraft.global.seed;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
@Component
@Slf4j
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.login-id}")
    private String adminLoginId;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email:}")
    private String adminEmail;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        seedAdminIfNecessary();
    }

    @Transactional
    public void seedAdminIfNecessary() {
        if (!StringUtils.hasText(adminLoginId) || !StringUtils.hasText(adminPassword)) {
            log.warn("Admin seed skipped: admin.login-id or admin.password not set");
            return;
        }

        if (adminRepository.existsByLoginId(adminLoginId)) {
            return;
        }

        String email = StringUtils.hasText(adminEmail)
                ? adminEmail
                : adminLoginId + "@example.com";

        try {
            adminRepository.save(
                    new Admin(
                            adminLoginId,
                            passwordEncoder.encode(adminPassword),
                            email
                    )
            );
        } catch (DataIntegrityViolationException ex) {
            log.warn("Admin seed skipped: admin user already exists");
        }
    }
}

package com.example.cowmjucraft.global.seed;

import com.example.cowmjucraft.domain.account.entity.Role;
import com.example.cowmjucraft.domain.account.entity.User;
import com.example.cowmjucraft.domain.account.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (!StringUtils.hasText(adminUserId) || !StringUtils.hasText(adminPassword)) {
            log.warn("Admin seed skipped: admin.user-id or admin.password not set");
            return;
        }

        if (userRepository.findByUserId(adminUserId).isPresent()) {
            return;
        }

        try {
            userRepository.save(
                    new User(
                            adminUserId,
                            "관리자",
                            adminUserId + "@example.com",
                            passwordEncoder.encode(adminPassword),
                            Role.ADMIN
                    )
            );
        } catch (DataIntegrityViolationException ex) {
            log.warn("Admin seed skipped: admin user already exists");
        }
    }
}

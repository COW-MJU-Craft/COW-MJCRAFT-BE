package com.example.cowmjucraft.global.seed;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 초기 관리자 계정을 시딩한다.
 *
 * <p>{@link AdminInitializer}와 별도 Bean으로 둔다. 같은 객체 안에서 호출하면
 * Spring proxy를 거치지 않아 {@code @Transactional}이 적용되지 않기 때문이다.
 */
@Component
@Slf4j
public class AdminSeeder {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminLoginId;
    private final String adminPassword;
    private final String adminEmail;

    public AdminSeeder(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.login-id}") String adminLoginId,
            @Value("${admin.password}") String adminPassword,
            @Value("${admin.email:}") String adminEmail
    ) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminLoginId = adminLoginId;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
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

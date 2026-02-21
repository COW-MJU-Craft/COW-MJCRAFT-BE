package com.example.cowmjucraft.domain.accounts.admin.auth.service;

import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(AdminLoginRequestDto request) {
        Admin admin = adminRepository.findByLoginId(request.userId())
                .orElseThrow(() -> new AccountException(AccountErrorType.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new AccountException(AccountErrorType.INVALID_CREDENTIALS);
        }

        String token = jwtTokenProvider.generateAdminToken(admin.getLoginId());

        return new LoginResult(admin, token, jwtTokenProvider.getExpirationSeconds());
    }

    public void logout() {
        // no-op: token state is not managed server-side yet.
    }

    public record LoginResult(
            Admin admin,
            String token,
            long expiresInSeconds
    ) {
    }
}

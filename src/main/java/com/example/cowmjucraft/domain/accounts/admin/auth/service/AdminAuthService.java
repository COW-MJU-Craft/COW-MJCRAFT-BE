package com.example.cowmjucraft.domain.accounts.admin.auth.service;

import com.example.cowmjucraft.domain.accounts.admin.auth.dto.request.AdminLoginRequestDto;
import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.admin.repository.AdminRepository;
import com.example.cowmjucraft.domain.accounts.auth.service.RefreshTokenService;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResult login(AdminLoginRequestDto request) {
        Admin admin = adminRepository.findByLoginId(request.userId())
                .orElseThrow(() -> new AccountException(AccountErrorType.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new AccountException(AccountErrorType.INVALID_CREDENTIALS);
        }

        RefreshTokenService.TokenPair tokenPair = refreshTokenService.issueTokenPair(admin.getLoginId(), Role.ROLE_ADMIN);
        return new LoginResult(admin, tokenPair);
    }

    public LoginResult refresh(String refreshToken) {
        RefreshTokenService.TokenPair tokenPair = refreshTokenService.refresh(refreshToken, Role.ROLE_ADMIN);
        Admin admin = adminRepository.findByLoginId(jwtTokenProvider.getSubject(tokenPair.accessToken()))
                .orElseThrow(() -> new AccountException(AccountErrorType.INVALID_CREDENTIALS));
        return new LoginResult(admin, tokenPair);
    }

    public void logout(String loginId) {
        refreshTokenService.revokeAllActiveBySubjectAndRole(loginId, Role.ROLE_ADMIN);
    }

    public record LoginResult(
            Admin admin,
            RefreshTokenService.TokenPair tokenPair
    ) {
    }
}

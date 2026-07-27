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
import com.example.cowmjucraft.global.security.CredentialMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final CredentialMatcher credentialMatcher;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResult login(AdminLoginRequestDto request) {
        // 계정이 없어도 해시 연산을 수행해 응답 시간으로 계정 존재 여부가 드러나지 않게 한다.
        Admin admin = adminRepository.findByLoginId(request.userId()).orElse(null);
        String storedHash = admin == null ? null : admin.getPassword();

        if (!credentialMatcher.matches(request.password(), storedHash)) {
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

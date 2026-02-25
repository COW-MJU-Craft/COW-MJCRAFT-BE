package com.example.cowmjucraft.domain.accounts.auth.service;

import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.auth.entity.RefreshToken;
import com.example.cowmjucraft.domain.accounts.auth.repository.RefreshTokenRepository;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenPair issueTokenPair(String subject, Role role) {
        LocalDateTime now = LocalDateTime.now();
        String accessToken = jwtTokenProvider.generateAccessToken(subject, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(subject, role);

        refreshTokenRepository.save(new RefreshToken(
                subject,
                role,
                hashToken(refreshToken),
                now.plusSeconds(jwtTokenProvider.getRefreshExpirationSeconds())
        ));

        return new TokenPair(
                accessToken,
                jwtTokenProvider.getAccessExpirationSeconds(),
                refreshToken,
                jwtTokenProvider.getRefreshExpirationSeconds()
        );
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken, Role expectedRole) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
        }
        if (!jwtTokenProvider.validateRefreshToken(rawRefreshToken)) {
            throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
        }

        String role = jwtTokenProvider.getRole(rawRefreshToken);
        if (role == null || !expectedRole.name().equals(role)) {
            throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
        }

        String subject = jwtTokenProvider.getSubject(rawRefreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN));

        LocalDateTime now = LocalDateTime.now();
        if (savedRefreshToken.getRevokedAt() != null) {
            throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
        }
        if (!savedRefreshToken.getExpiresAt().isAfter(now)) {
            throw new AccountException(AccountErrorType.REFRESH_TOKEN_EXPIRED);
        }

        savedRefreshToken.revoke(now);
        return issueTokenPair(subject, expectedRole);
    }

    @Transactional
    public void revokeAllActiveBySubjectAndRole(String subject, Role role) {
        if (subject == null || subject.isBlank() || role == null) {
            return;
        }
        refreshTokenRepository.revokeActiveTokens(subject, role, LocalDateTime.now());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    public record TokenPair(
            String accessToken,
            long accessTokenExpiresInSeconds,
            String refreshToken,
            long refreshTokenExpiresInSeconds
    ) {
    }
}

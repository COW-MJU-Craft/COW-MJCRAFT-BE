package com.example.cowmjucraft.domain.accounts.auth.service;

import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.auth.entity.RefreshToken;
import com.example.cowmjucraft.domain.accounts.auth.repository.RefreshTokenRepository;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
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
        String tokenHash = hashToken(rawRefreshToken);
        LocalDateTime now = LocalDateTime.now();

        // 조회-검사-변경이 아니라 조건부 UPDATE로 소비한다. 동시 요청이 들어와도
        // DB가 한 번만 성공시키므로 하나의 토큰에서 두 세션이 파생되지 않는다.
        if (refreshTokenRepository.consumeIfActive(tokenHash, now) == 0) {
            handleUnusableToken(tokenHash, subject);
        }

        RefreshToken consumed = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN));

        // 만료 토큰은 이미 폐기된 상태로 남는다 — 재사용 여지를 없애는 편이 안전하다.
        if (!consumed.getExpiresAt().isAfter(now)) {
            throw new AccountException(AccountErrorType.REFRESH_TOKEN_EXPIRED);
        }

        return issueTokenPair(subject, expectedRole);
    }

    /**
     * 소비에 실패한 토큰을 분류한다.
     *
     * <p>이미 폐기된 토큰이 다시 등장하는 것은 회전형 방식에서 탈취를 의심할 근거다.
     * 정상 사용자는 폐기된 토큰을 다시 쓸 일이 없다. 해당 주체의 활성 토큰을 모두
     * 무효화해 공격자와 정상 사용자 양쪽의 세션을 끊는다.
     */
    private void handleUnusableToken(String tokenHash, String subject) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);

        if (existing == null) {
            throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
        }

        log.warn(
                "폐기된 리프레시 토큰 재사용 감지 — 해당 주체의 활성 토큰을 모두 폐기합니다. subject={}, role={}",
                subject,
                existing.getRole()
        );
        refreshTokenRepository.revokeActiveTokens(
                existing.getSubject(),
                existing.getRole(),
                LocalDateTime.now()
        );

        throw new AccountException(AccountErrorType.INVALID_REFRESH_TOKEN);
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

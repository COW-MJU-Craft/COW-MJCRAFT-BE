package com.example.cowmjucraft.domain.accounts.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cowmjucraft.domain.accounts.Role;
import com.example.cowmjucraft.domain.accounts.auth.entity.RefreshToken;
import com.example.cowmjucraft.domain.accounts.auth.repository.RefreshTokenRepository;
import com.example.cowmjucraft.domain.accounts.exception.AccountErrorType;
import com.example.cowmjucraft.domain.accounts.exception.AccountException;
import com.example.cowmjucraft.global.config.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceTest {

    private static final String RAW_TOKEN = "raw-refresh-token";
    private static final String SUBJECT = "admin";

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        when(jwtTokenProvider.validateRefreshToken(RAW_TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getRole(RAW_TOKEN)).thenReturn(Role.ROLE_ADMIN.name());
        when(jwtTokenProvider.getSubject(RAW_TOKEN)).thenReturn(SUBJECT);
        when(jwtTokenProvider.generateAccessToken(anyString(), any())).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(anyString(), any())).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessExpirationSeconds()).thenReturn(900L);
        when(jwtTokenProvider.getRefreshExpirationSeconds()).thenReturn(1_209_600L);
    }

    @Test
    void refresh_유효한토큰_소비후새토큰쌍발급() {
        // given
        when(refreshTokenRepository.consumeIfActive(anyString(), any())).thenReturn(1);
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(activeToken(LocalDateTime.now().plusDays(7))));

        // when
        RefreshTokenService.TokenPair pair = refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN);

        // then
        assertThat(pair.accessToken()).isEqualTo("new-access");
        assertThat(pair.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenRepository).consumeIfActive(anyString(), any());
        verify(refreshTokenRepository, never()).revokeActiveTokens(anyString(), any(), any());
    }

    @Test
    void refresh_이미소비된토큰_재사용으로보고전체세션폐기() {
        // given — 조건부 UPDATE가 0을 반환하고, 조회하면 폐기된 토큰이 존재한다
        when(refreshTokenRepository.consumeIfActive(anyString(), any())).thenReturn(0);
        RefreshToken revoked = activeToken(LocalDateTime.now().plusDays(7));
        revoked.revoke(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class)
                .extracting(e -> ((AccountException) e).getErrorCode())
                .isEqualTo(AccountErrorType.INVALID_REFRESH_TOKEN);

        // 탈취 신호로 보고 해당 주체의 활성 토큰을 모두 폐기해야 한다
        verify(refreshTokenRepository).revokeActiveTokens(eq(SUBJECT), eq(Role.ROLE_ADMIN), any());
    }

    @Test
    void refresh_존재하지않는토큰_전체폐기없이예외() {
        // given
        when(refreshTokenRepository.consumeIfActive(anyString(), any())).thenReturn(0);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class);

        // 없는 토큰은 탈취 신호가 아니므로 다른 세션을 끊으면 안 된다
        verify(refreshTokenRepository, never()).revokeActiveTokens(anyString(), any(), any());
    }

    @Test
    void refresh_만료된토큰_REFRESH_TOKEN_EXPIRED발생() {
        // given
        when(refreshTokenRepository.consumeIfActive(anyString(), any())).thenReturn(1);
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(activeToken(LocalDateTime.now().minusSeconds(1))));

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class)
                .extracting(e -> ((AccountException) e).getErrorCode())
                .isEqualTo(AccountErrorType.REFRESH_TOKEN_EXPIRED);
    }

    @Test
    void refresh_역할이다른토큰_소비시도없이예외() {
        // given
        when(jwtTokenProvider.getRole(RAW_TOKEN)).thenReturn("ROLE_USER");

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class);

        verify(refreshTokenRepository, never()).consumeIfActive(anyString(), any());
    }

    @Test
    void refresh_서명이유효하지않은토큰_소비시도없이예외() {
        // given
        when(jwtTokenProvider.validateRefreshToken(RAW_TOKEN)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> refreshTokenService.refresh(RAW_TOKEN, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class);

        verify(refreshTokenRepository, never()).consumeIfActive(anyString(), any());
    }

    @Test
    void refresh_토큰이비어있으면_예외() {
        assertThatThrownBy(() -> refreshTokenService.refresh("  ", Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class);
        assertThatThrownBy(() -> refreshTokenService.refresh(null, Role.ROLE_ADMIN))
                .isInstanceOf(AccountException.class);
    }

    private RefreshToken activeToken(LocalDateTime expiresAt) {
        return new RefreshToken(SUBJECT, Role.ROLE_ADMIN, "hash", expiresAt);
    }
}

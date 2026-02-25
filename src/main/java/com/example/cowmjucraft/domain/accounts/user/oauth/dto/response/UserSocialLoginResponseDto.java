package com.example.cowmjucraft.domain.accounts.user.oauth.dto.response;

import com.example.cowmjucraft.domain.accounts.auth.service.RefreshTokenService;
import com.example.cowmjucraft.domain.accounts.user.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserSocialLoginResponseDto(
        Long memberId,
        String userName,
        String email,
        @Schema(description = "Swagger Authorize에 입력할 Access Token (Bearer 제외)")
        String accessToken,
        long accessTokenExpiresInSeconds,
        @Schema(description = "Access Token 재발급에 사용하는 Refresh Token")
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
    public static UserSocialLoginResponseDto from(Member member, RefreshTokenService.TokenPair tokenPair) {
        return new UserSocialLoginResponseDto(
                member.getId(),
                member.getUserName(),
                member.getEmail(),
                tokenPair.accessToken(),
                tokenPair.accessTokenExpiresInSeconds(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresInSeconds()
        );
    }
}

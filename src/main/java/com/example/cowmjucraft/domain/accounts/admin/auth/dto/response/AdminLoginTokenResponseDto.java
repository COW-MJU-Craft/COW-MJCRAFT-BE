package com.example.cowmjucraft.domain.accounts.admin.auth.dto.response;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import com.example.cowmjucraft.domain.accounts.auth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginTokenResponseDto(
        String loginId,
        String email,
        @Schema(description = "Swagger Authorize에 입력할 Access Token (Bearer 제외)")
        String accessToken,
        long accessTokenExpiresInSeconds,
        @Schema(description = "Access Token 재발급에 사용하는 Refresh Token")
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
    public static AdminLoginTokenResponseDto from(Admin admin, RefreshTokenService.TokenPair tokenPair) {
        return new AdminLoginTokenResponseDto(
                admin.getLoginId(),
                admin.getEmail(),
                tokenPair.accessToken(),
                tokenPair.accessTokenExpiresInSeconds(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresInSeconds()
        );
    }
}

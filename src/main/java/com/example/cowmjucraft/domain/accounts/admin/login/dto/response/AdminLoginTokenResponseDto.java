package com.example.cowmjucraft.domain.accounts.admin.login.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginTokenResponseDto(
        String loginId,
        String email,
        @Schema(description = "Swagger Authorize에 입력할 Access Token (Bearer 제외)")
        String accessToken
) {
    public static AdminLoginTokenResponseDto from(AdminLoginResponseDto admin, String accessToken) {
        return new AdminLoginTokenResponseDto(admin.loginId(), admin.email(), accessToken);
    }
}

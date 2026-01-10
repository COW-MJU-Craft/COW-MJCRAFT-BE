package com.example.cowmjucraft.domain.accounts.admin.auth.dto.response;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginTokenResponseDto(
        String loginId,
        String email,
        @Schema(description = "Swagger Authorize에 입력할 Access Token (Bearer 제외)")
        String accessToken
) {
    public static AdminLoginTokenResponseDto from(Admin admin, String accessToken) {
        return new AdminLoginTokenResponseDto(admin.getLoginId(), admin.getEmail(), accessToken);
    }
}

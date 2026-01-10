package com.example.cowmjucraft.domain.accounts.admin.login.dto.response;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminLoginResponseDto(
        String loginId,
        String email,
        String accessToken
) {
    public static AdminLoginResponseDto from(Admin admin, String accessToken) {
        return new AdminLoginResponseDto(admin.getLoginId(), admin.getEmail(), accessToken);
    }
}

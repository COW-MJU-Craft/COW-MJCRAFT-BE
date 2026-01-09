package com.example.cowmjucraft.domain.accounts.admin.login.dto.response;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;

public record AdminLoginResponseDto(
        String loginId,
        String email
) {
    public static AdminLoginResponseDto from(Admin admin) {
        return new AdminLoginResponseDto(admin.getLoginId(), admin.getEmail());
    }
}

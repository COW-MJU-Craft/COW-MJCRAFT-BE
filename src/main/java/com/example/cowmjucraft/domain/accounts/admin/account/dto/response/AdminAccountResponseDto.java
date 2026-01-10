package com.example.cowmjucraft.domain.accounts.admin.account.dto.response;

import com.example.cowmjucraft.domain.accounts.admin.entity.Admin;

public record AdminAccountResponseDto(
        String loginId,
        String email
) {
    public static AdminAccountResponseDto from(Admin admin) {
        return new AdminAccountResponseDto(admin.getLoginId(), admin.getEmail());
    }
}

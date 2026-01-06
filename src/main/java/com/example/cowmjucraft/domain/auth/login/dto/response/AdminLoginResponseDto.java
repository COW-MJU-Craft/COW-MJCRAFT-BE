package com.example.cowmjucraft.domain.auth.login.dto.response;

import com.example.cowmjucraft.domain.account.entity.Role;
import com.example.cowmjucraft.domain.account.entity.User;

public record AdminLoginResponseDto(
        String userId,
        Role role
) {
    public static AdminLoginResponseDto from(User user) {
        return new AdminLoginResponseDto(user.getUserId(), user.getRole());
    }
}

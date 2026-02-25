package com.example.cowmjucraft.domain.accounts.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(
        @NotBlank(message = "refreshToken은 필수입니다.")
        String refreshToken
) {
}

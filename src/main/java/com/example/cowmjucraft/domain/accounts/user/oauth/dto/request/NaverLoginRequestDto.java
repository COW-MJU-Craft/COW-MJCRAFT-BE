package com.example.cowmjucraft.domain.accounts.user.oauth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NaverLoginRequestDto(
        @NotBlank String code,
        String state
) {
}

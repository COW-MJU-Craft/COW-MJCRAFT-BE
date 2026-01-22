package com.example.cowmjucraft.domain.accounts.user.oauth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequestDto(
        @NotBlank String code
) {
}

package com.example.cowmjucraft.domain.accounts.user.oauth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSocialLoginResponseDto(
        String userId,
        String email,
        @Schema(description = "Swagger Authorize에 입력할 Access Token (Bearer 제외)")
        String accessToken
) {
}

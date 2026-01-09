package com.example.cowmjucraft.domain.accounts.admin.login.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 로그인 요청")
public record AdminLoginRequestDto(
        @Schema(example = "admin")
        @NotBlank
        String userId,
        @Schema(example = "admin1234")
        @NotBlank
        String password
) {
}

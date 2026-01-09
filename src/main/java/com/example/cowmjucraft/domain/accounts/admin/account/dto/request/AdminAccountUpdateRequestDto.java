package com.example.cowmjucraft.domain.accounts.admin.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 계정 수정 요청")
public record AdminAccountUpdateRequestDto(
        @Schema(example = "admin")
        @NotBlank
        String currentUserId,

        @Schema(example = "admin1234")
        @NotBlank
        String currentPassword,

        @Schema(example = "new-admin")
        @NotBlank
        String newUserId,
        
        @Schema(example = "new-password1234")
        String newPassword
) {
}

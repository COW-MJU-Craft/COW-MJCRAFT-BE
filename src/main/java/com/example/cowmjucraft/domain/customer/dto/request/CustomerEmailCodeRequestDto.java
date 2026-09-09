package com.example.cowmjucraft.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증 코드 발송 요청 DTO")
public record CustomerEmailCodeRequestDto(

        @NotBlank(message = "email은 필수입니다.")
        @Schema(description = "이메일", example = "yunjin@mju.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) {
}

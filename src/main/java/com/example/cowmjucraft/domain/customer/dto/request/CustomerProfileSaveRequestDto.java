package com.example.cowmjucraft.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "고객 프로필 저장 요청 DTO")
public record CustomerProfileSaveRequestDto(

        @NotBlank(message = "email은 필수입니다.")
        @Schema(description = "이메일", example = "yunjin@mju.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "password는 필수입니다.")
        @Schema(description = "비밀번호", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Valid
        @NotNull(message = "profile은 필수입니다.")
        @Schema(description = "저장할 프로필", requiredMode = Schema.RequiredMode.REQUIRED)
        CustomerProfileRequestDto profile
) {
}

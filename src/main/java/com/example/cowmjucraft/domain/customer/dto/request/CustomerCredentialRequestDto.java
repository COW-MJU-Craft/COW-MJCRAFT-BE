package com.example.cowmjucraft.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 자격증명은 항상 요청 body로 받는다. 쿼리 스트링에 이메일·비밀번호를 넣지 않기
 * 위해 조회성 요청도 POST를 쓴다.
 */
@Schema(description = "고객 자격증명 요청 DTO")
public record CustomerCredentialRequestDto(

        @NotBlank(message = "email은 필수입니다.")
        @Schema(description = "이메일", example = "yunjin@mju.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "password는 필수입니다.")
        @Schema(description = "비밀번호", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}

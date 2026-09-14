package com.example.cowmjucraft.domain.customer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * 정보 등록과 비밀번호 재설정을 겸한다.
 *
 * <p>서버가 하는 일이 양쪽 모두 "코드로 이메일 소유를 증명하고 비밀번호를 저장"으로
 * 동일하다. 엔드포인트를 나누면 응답 차이로 계정 존재 여부만 드러난다.
 */
@Schema(description = "고객 정보 등록 · 비밀번호 재설정 요청 DTO")
public record CustomerEnrollRequestDto(

        @NotBlank(message = "email은 필수입니다.")
        @Schema(description = "이메일", example = "yunjin@mju.ac.kr", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @NotBlank(message = "code는 필수입니다.")
        @Schema(description = "이메일로 받은 6자리 코드", example = "481902", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @NotBlank(message = "password는 필수입니다.")
        @Schema(description = "설정할 비밀번호", example = "Pa$$w0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,

        @Valid
        @Schema(description = "함께 저장할 프로필. 생략하면 프로필은 변경하지 않는다(재설정 시나리오)")
        CustomerProfileRequestDto profile
) {
}

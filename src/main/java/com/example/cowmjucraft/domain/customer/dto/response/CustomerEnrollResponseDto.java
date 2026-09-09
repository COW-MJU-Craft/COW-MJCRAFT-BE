package com.example.cowmjucraft.domain.customer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 정보 등록 · 비밀번호 재설정 응답 DTO")
public record CustomerEnrollResponseDto(

        @Schema(description = "정규화된 이메일", example = "yunjin@mju.ac.kr")
        String email,

        @Schema(description = "직접 저장한 프로필 보유 여부", example = "true")
        boolean profileSaved
) {
}

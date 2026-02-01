package com.example.cowmjucraft.domain.introduce.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소개 목적(Purpose)")
public record IntroducePurposeDto(
        @Schema(description = "제목", example = "우리가 하는 일", nullable = true)
        String title,

        @Schema(
                description = "설명",
                example = "명지공방은 학생들이 직접 기획하고 제작하는 굿즈 프로젝트를 운영합니다.",
                nullable = true
        )
        String description
) {
}

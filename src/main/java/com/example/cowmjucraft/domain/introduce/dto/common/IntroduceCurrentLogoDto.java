package com.example.cowmjucraft.domain.introduce.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소개 현재 로고 정보")
public record IntroduceCurrentLogoDto(
        @Schema(description = "제목", example = "현재 로고", nullable = true)
        String title,

        @Schema(
                description = "이미지 S3 object key",
                example = "uploads/introduce/sections/uuid-current-logo.png",
                nullable = true
        )
        String imageKey,

        @Schema(description = "설명", example = "현재 로고는 ...", nullable = true)
        String description
) {
}

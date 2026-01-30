package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로고 히스토리 응답")
public record IntroduceLogoHistoryResponseDto(
        @Schema(description = "연도", example = "2024")
        String year,

        @Schema(
                description = "이미지 S3 object key",
                example = "uploads/introduce/sections/uuid-logo-2024.png",
                nullable = true
        )
        String imageKey,

        @Schema(description = "이미지 접근 URL", example = "https://bucket.s3.amazonaws.com/...", nullable = true)
        String imageUrl,

        @Schema(description = "설명", example = "심볼 개선 및 색상 정리", nullable = true)
        String description
) {
}

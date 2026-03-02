package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 로고 관리자 응답")
public record AdminIntroduceCurrentLogoResponseDto(
        @Schema(description = "제목", example = "현재 로고")
        String title,

        @Schema(description = "이미지 S3 object key", example = "uploads/introduce/sections/uuid-current-logo.png", nullable = true)
        String imageKey,

        @Schema(description = "이미지 접근 URL", example = "https://bucket.s3.amazonaws.com/...", nullable = true)
        String imageUrl,

        @Schema(description = "설명", example = "현재 로고는 2025년 리브랜딩을 통해 확정되었습니다.", nullable = true)
        String description
) {
}

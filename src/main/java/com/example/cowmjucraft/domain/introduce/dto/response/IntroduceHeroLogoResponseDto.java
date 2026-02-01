package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메인 히어로 로고(표시용)")
public record IntroduceHeroLogoResponseDto(
        @Schema(description = "S3 object key", example = "uploads/introduce/hero-logos/uuid.png")
        String key,

        @Schema(description = "이미지 접근 URL(presigned)", example = "https://...aws...signature...", nullable = true)
        String imageUrl
) {}
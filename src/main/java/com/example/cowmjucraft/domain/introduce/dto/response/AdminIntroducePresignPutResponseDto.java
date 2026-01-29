package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소개 이미지 presign-put 응답")
public record AdminIntroducePresignPutResponseDto(

        @Schema(description = "S3 object key", example = "uploads/introduce/hero-logos/uuid-hero-logo.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/uploads/introduce/hero-logos/uuid-hero-logo.png?...signature...")
        String uploadUrl,

        @Schema(description = "presign URL 만료 시간(초)", example = "300")
        int expiresInSeconds
) {
}

package com.example.cowmjucraft.domain.media.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MediaPresignPutResponseDto(
        @Schema(description = "S3 object key", example = "uploads/intro/uuid-intro-banner.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "presign URL 만료 시간(초)", example = "300")
        long expiresInSeconds
) {
}

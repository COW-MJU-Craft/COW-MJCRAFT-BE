package com.example.cowmjucraft.domain.media.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminMediaPresignResponseDto(
        @Schema(description = "S3 object key", example = "uploads/uuid-intro-banner.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "presign 만료 시간(초)", example = "300")
        Long expiresInSeconds
) {
}

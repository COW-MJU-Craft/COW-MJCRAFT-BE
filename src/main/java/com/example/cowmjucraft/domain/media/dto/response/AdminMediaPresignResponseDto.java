package com.example.cowmjucraft.domain.media.dto.response;

public record AdminMediaPresignResponseDto(
        @io.swagger.v3.oas.annotations.media.Schema(description = "생성된 미디어 ID", example = "1")
        Long mediaId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "S3 object key", example = "uploads/uuid-intro-banner.png")
        String key,
        @io.swagger.v3.oas.annotations.media.Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,
        @io.swagger.v3.oas.annotations.media.Schema(description = "presign 만료 시간(초)", example = "300")
        Long expiresInSeconds
) {
}

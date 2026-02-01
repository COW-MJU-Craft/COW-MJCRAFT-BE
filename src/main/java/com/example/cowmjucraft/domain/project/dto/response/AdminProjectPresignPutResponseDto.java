package com.example.cowmjucraft.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 썸네일 presign-put 응답")
public record AdminProjectPresignPutResponseDto(

        @Schema(description = "S3 object key", example = "uploads/projects/thumbnails/uuid-thumbnail.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "presign URL 만료 시간(초)", example = "300")
        int expiresInSeconds
) {
}

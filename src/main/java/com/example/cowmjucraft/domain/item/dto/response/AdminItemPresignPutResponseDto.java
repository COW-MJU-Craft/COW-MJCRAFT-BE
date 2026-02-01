package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "물품 이미지 presign-put 응답")
public record AdminItemPresignPutResponseDto(

        @Schema(description = "S3 object key", example = "uploads/items/1/images/uuid-detail.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "presign URL 만료 시간(초)", example = "300")
        int expiresInSeconds
) {
}

package com.example.cowmjucraft.domain.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 이미지 presign-put 응답")
public record AdminNoticePresignPutResponseDto(

        @Schema(description = "S3 object key", example = "uploads/notices/images/uuid-notice.png")
        String key,

        @Schema(description = "S3 PUT 업로드용 presigned URL", example = "https://bucket.s3.amazonaws.com/...")
        String uploadUrl,

        @Schema(description = "presign URL 만료 시간(초)", example = "300")
        int expiresInSeconds
) {
}

package com.example.cowmjucraft.domain.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지사항 목록 관리자 응답")
public record AdminNoticeSummaryResponseDto(

        @Schema(description = "공지 ID", example = "1")
        Long id,

        @Schema(description = "공지 제목", example = "설 연휴 배송 일정 안내")
        String title,

        @Schema(description = "공지 이미지 S3 object key 목록", example = "[\"uploads/notices/images/uuid-notice.png\"]")
        List<String> imageKeys,

        @Schema(description = "공지 이미지 presigned URL 목록", example = "[\"https://bucket.s3.amazonaws.com/uploads/notices/images/uuid-notice.png?X-Amz-Signature=...\"]")
        List<String> imageUrls,

        @Schema(description = "등록일시", example = "2026-01-20T10:15:30")
        LocalDateTime createdAt
) {
}

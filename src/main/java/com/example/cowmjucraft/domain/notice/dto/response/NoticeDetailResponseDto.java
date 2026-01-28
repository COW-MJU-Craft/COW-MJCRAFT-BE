package com.example.cowmjucraft.domain.notice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지사항 상세 응답")
public record NoticeDetailResponseDto(

        @Schema(description = "공지 ID", example = "1")
        Long id,

        @Schema(description = "공지 제목", example = "설 연휴 배송 일정 안내")
        String title,

        @Schema(description = "공지 내용", example = "설 연휴 기간 동안 배송이 중단됩니다.")
        String content,

        @Schema(description = "공지 이미지 S3 key 목록", example = "[\"uploads/notices/notice-001.png\"]")
        List<String> imageKeys,

        @Schema(description = "등록일시", example = "2026-01-20T10:15:30")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-01-22T09:00:00")
        LocalDateTime updatedAt
) {
}

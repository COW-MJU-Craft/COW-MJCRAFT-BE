package com.example.cowmjucraft.domain.project.dto.response;

import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "프로젝트 목록 아이템 응답")
public record ProjectListItemResponseDto(

        @Schema(description = "프로젝트 ID", example = "1")
        Long id,

        @Schema(description = "프로젝트 제목", example = "명지공방 머그컵 프로젝트")
        String title,

        @Schema(description = "리스트 한줄 소개", example = "캠퍼스 감성을 담은 머그컵을 제작합니다.")
        String summary,

        @Schema(
                description = "썸네일 S3 key",
                example = "uploads/projects/thumbnail-001.png",
                deprecated = true
        )
        String thumbnailKey,

        @Schema(description = "썸네일 이미지 URL", example = "https://bucket.s3.amazonaws.com/uploads/projects/thumbnail-001.png?X-Amz-Signature=...")
        String thumbnailUrl,

        @Schema(description = "프로젝트 상태", example = "OPEN")
        ProjectStatus status,

        @Schema(description = "마감일 (YYYY-MM-DD)", example = "2026-03-15")
        LocalDate deadlineDate,

        @Schema(description = "D-day (마감일까지 남은 일수)", example = "30")
        long dDay,

        @Schema(description = "고정 여부", example = "true")
        Boolean pinned
) {
}

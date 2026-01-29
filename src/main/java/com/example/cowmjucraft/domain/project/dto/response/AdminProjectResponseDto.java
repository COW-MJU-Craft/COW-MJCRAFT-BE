package com.example.cowmjucraft.domain.project.dto.response;

import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "프로젝트 관리자 응답")
public record AdminProjectResponseDto(

        @Schema(description = "프로젝트 ID", example = "1")
        Long id,

        @Schema(description = "프로젝트 제목", example = "명지공방 머그컵 프로젝트")
        String title,

        @Schema(description = "리스트 한줄 소개", example = "캠퍼스 감성을 담은 머그컵을 제작합니다.")
        String summary,

        @Schema(description = "프로젝트 상세 설명", example = "학생들이 함께 디자인한 머그컵 프로젝트입니다.")
        String description,

        @Schema(description = "썸네일 S3 key", example = "uploads/projects/thumbnail-001.png")
        String thumbnailKey,

        @Schema(description = "프로젝트 상세 이미지 S3 object key 목록(정렬 순서대로)", example = "[\"uploads/projects/images/uuid-01.png\", \"uploads/projects/images/uuid-02.png\"]")
        List<String> imageKeys,

        @Schema(description = "프로젝트 상태", example = "OPEN")
        ProjectStatus status,

        @Schema(description = "마감일 (YYYY-MM-DD)", example = "2026-03-15")
        LocalDate deadlineDate,

        @Schema(description = "고정 여부", example = "false")
        boolean pinned,

        @Schema(description = "고정 순서", example = "1")
        Integer pinnedOrder,

        @Schema(description = "수동 정렬 순서", example = "3")
        Integer manualOrder,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
}

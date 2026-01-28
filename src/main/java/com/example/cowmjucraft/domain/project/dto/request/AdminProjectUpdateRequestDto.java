package com.example.cowmjucraft.domain.project.dto.request;

import com.example.cowmjucraft.domain.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "프로젝트 수정 요청")
public record AdminProjectUpdateRequestDto(

        @NotBlank
        @Size(max = 100)
        @Schema(description = "프로젝트 제목", example = "명지공방 머그컵 프로젝트")
        String title,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "리스트 한줄 소개", example = "캠퍼스 감성을 담은 머그컵을 제작합니다.")
        String summary,

        @NotBlank
        @Schema(description = "프로젝트 상세 설명", example = "학생들이 함께 디자인한 머그컵 프로젝트입니다.")
        String description,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "썸네일 S3 key", example = "uploads/projects/thumbnail-001.png")
        String thumbnailKey,

        @NotNull
        @Schema(description = "마감일 (YYYY-MM-DD)", example = "2026-03-15")
        LocalDate deadlineDate,

        @NotNull
        @Schema(description = "프로젝트 상태", example = "OPEN")
        ProjectStatus status
) {
}

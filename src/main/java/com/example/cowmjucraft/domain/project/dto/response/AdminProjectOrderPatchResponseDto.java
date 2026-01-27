package com.example.cowmjucraft.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트 고정/정렬 변경 응답")
public record AdminProjectOrderPatchResponseDto(

        @Schema(description = "고정 순서 재부여 개수", example = "3")
        int updatedPinnedCount,

        @Schema(description = "수동 정렬 변경 개수", example = "2")
        int updatedManualCount
) {
}

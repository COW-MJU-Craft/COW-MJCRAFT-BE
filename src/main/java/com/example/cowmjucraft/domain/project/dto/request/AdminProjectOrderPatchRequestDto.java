package com.example.cowmjucraft.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "프로젝트 고정/정렬 일괄 변경 요청")
public record AdminProjectOrderPatchRequestDto(

        @NotNull
        @NotEmpty
        @Valid
        @Schema(description = "정렬 변경 항목 목록 (빈 리스트 불가)")
        List<ItemDto> items
) {

    @Schema(description = "정렬 변경 항목")
    public record ItemDto(

            @NotNull
            @Schema(description = "프로젝트 ID", example = "3")
            Long projectId,

            @NotNull
            @Schema(description = "고정 여부", example = "true")
            Boolean pinned,

            @Schema(description = "고정 정렬 순서 (pinned=true인 경우만 적용)", example = "1")
            Integer pinnedOrder,

            @Schema(description = "수동 정렬 순서 (pinned=false인 경우만 적용)", example = "1")
            Integer manualOrder
    ) {
    }
}

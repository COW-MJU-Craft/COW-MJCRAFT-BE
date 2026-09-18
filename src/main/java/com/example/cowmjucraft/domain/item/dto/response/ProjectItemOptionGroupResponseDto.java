package com.example.cowmjucraft.domain.item.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "프로젝트 물품 옵션 그룹 응답")
public record ProjectItemOptionGroupResponseDto(
        @Schema(description = "옵션 그룹 ID") Long id,
        @Schema(description = "옵션 그룹명") String name,
        @Schema(description = "필수 선택 여부") boolean required,
        @Schema(description = "옵션 값 목록") List<ProjectItemOptionValueResponseDto> values
) {
}

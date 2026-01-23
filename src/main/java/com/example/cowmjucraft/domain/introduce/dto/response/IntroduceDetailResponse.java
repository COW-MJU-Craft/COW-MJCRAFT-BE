package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "소개 상세 응답 DTO")
public record IntroduceDetailResponse(

        @Schema(description = "소개 섹션 목록 (type 필드 포함)")
        List<Map<String, Object>> sections
) {
}

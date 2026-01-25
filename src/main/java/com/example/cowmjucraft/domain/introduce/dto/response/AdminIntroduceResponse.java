package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "소개 관리자 조회 응답 DTO")
public record AdminIntroduceResponse(

        @Schema(description = "제목", example = "명지공방 소개")
        String title,

        @Schema(description = "부제목", example = "우리의 방향과 비전")
        String subtitle,

        @Schema(description = "요약", example = "명지공방은 ...")
        String summary,

        @Schema(description = "메인 히어로 로고 S3 object key 목록")
        List<String> heroLogoKeys,

        @Schema(description = "소개 섹션 목록 (type 필드 포함)")
        List<Map<String, Object>> sections,

        @Schema(description = "최종 수정 시각")
        LocalDateTime updatedAt
) {
}

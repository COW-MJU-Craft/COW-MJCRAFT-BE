package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "소개 관리자 조회 응답 DTO")
public record AdminIntroduceResponseDto(

        @Schema(description = "제목", example = "명지공방 소개")
        String title,

        @Schema(description = "부제목", example = "우리의 방향과 비전")
        String subtitle,

        @Schema(description = "요약", example = "명지공방은 학생들이 직접 만드는 공방 브랜드입니다.")
        String summary,

        @Schema(
                description = "메인 히어로 로고 S3 object key 목록",
                example = "[\"uploads/introduce/hero-logos/uuid-logo-1.png\", \"uploads/introduce/hero-logos/uuid-logo-2.png\"]"
        )
        List<String> heroLogoKeys,

        @Schema(
                description = "소개 섹션 목록 (type 필드 포함)",
                example = """
                        [
                          {
                            "type": "HEADER",
                            "title": "명지공방",
                            "subtitle": "학생들이 만드는 작은 브랜드",
                            "backgroundImageKey": "uploads/introduce/sections/uuid-hero-bg.png"
                          }
                        ]
                        """
        )
        List<Map<String, Object>> sections,

        @Schema(description = "최종 수정 시각", example = "2026-01-29T00:12:34")
        LocalDateTime updatedAt
) {
}

package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "소개 메인 관리자 응답 DTO")
public record AdminIntroduceMainResponseDto(
        @Schema(description = "브랜드명", example = "명지공방")
        String title,

        @Schema(description = "슬로건", example = "학생들이 만드는 작은 브랜드", nullable = true)
        String subtitle,

        @Schema(description = "한 줄 요약", example = "명지공방은 학생들이 직접 만드는 공방 브랜드입니다.", nullable = true)
        String summary,

        @Schema(description = "메인 히어로 로고 목록(key + url)")
        List<IntroduceHeroLogoResponseDto> heroLogos,

        @Schema(description = "최종 수정 시각", example = "2026-01-29T00:12:34")
        LocalDateTime updatedAt
) {
}
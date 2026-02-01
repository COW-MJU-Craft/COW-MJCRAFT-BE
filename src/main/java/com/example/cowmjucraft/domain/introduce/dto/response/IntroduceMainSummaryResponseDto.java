package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "소개 메인 요약 응답 DTO")
public record IntroduceMainSummaryResponseDto(
        @Schema(description = "브랜드명", example = "명지공방")
        String title,

        @Schema(description = "슬로건", example = "우리의 손끝에서, 명지가 피어납니다.")
        String subtitle,

        @Schema(description = "요약", example = "간단한 소개", nullable = true)
        String summary,

        @Schema(description = "히어로 로고 목록(표시용 url 포함)")
        List<IntroduceHeroLogoResponseDto> heroLogos
) {
}
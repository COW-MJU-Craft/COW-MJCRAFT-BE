package com.example.cowmjucraft.domain.introduce.dto.response;

import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceCurrentLogoDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceIntroDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceLogoHistoryDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroducePurposeDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "소개 상세 관리자 응답 DTO")
public record AdminIntroduceDetailResponseDto(
        @Schema(description = "긴 소개 본문", nullable = true)
        IntroduceIntroDto intro,

        @Schema(description = "목적(Purpose)")
        IntroducePurposeDto purpose,

        @Schema(description = "현재 로고")
        IntroduceCurrentLogoDto currentLogo,

        @Schema(description = "로고 히스토리 목록")
        List<IntroduceLogoHistoryDto> logoHistories,

        @Schema(description = "최종 수정 시각", example = "2026-01-29T00:12:34")
        LocalDateTime updatedAt
) {
}

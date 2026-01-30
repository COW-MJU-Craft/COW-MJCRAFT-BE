package com.example.cowmjucraft.domain.introduce.dto.response;

import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceIntroDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroducePurposeDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "소개 상세 응답 DTO")
public record IntroduceDetailResponseDto(
        @Schema(description = "브랜드명/슬로건")
        IntroduceBrandResponseDto brand,

        @Schema(description = "긴 소개 본문")
        IntroduceIntroDto intro,

        @Schema(description = "목적(Purpose)")
        IntroducePurposeDto purpose,

        @Schema(description = "현재 로고")
        IntroduceCurrentLogoResponseDto currentLogo,

        @Schema(description = "로고 히스토리 목록")
        List<IntroduceLogoHistoryResponseDto> logoHistories
) {
}

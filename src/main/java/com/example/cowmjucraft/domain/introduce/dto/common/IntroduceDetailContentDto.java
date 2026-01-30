package com.example.cowmjucraft.domain.introduce.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "소개 상세 저장용 컨텐츠(JSON)")
public record IntroduceDetailContentDto(
        @Schema(description = "긴 소개 본문", nullable = true)
        IntroduceIntroDto intro,

        @Schema(description = "목적(Purpose)", nullable = true)
        IntroducePurposeDto purpose,

        @Schema(description = "현재 로고", nullable = true)
        IntroduceCurrentLogoDto currentLogo,

        @Schema(description = "로고 히스토리 목록", nullable = true)
        List<IntroduceLogoHistoryDto> logoHistories
) {
    public static IntroduceDetailContentDto empty() {
        return new IntroduceDetailContentDto(
                new IntroduceIntroDto(null, null, null),
                new IntroducePurposeDto("", null),
                new IntroduceCurrentLogoDto("", null, null),
                List.of()
        );
    }
}

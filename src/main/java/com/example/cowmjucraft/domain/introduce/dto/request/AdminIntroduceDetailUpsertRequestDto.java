package com.example.cowmjucraft.domain.introduce.dto.request;

import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceCurrentLogoDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceIntroDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroduceLogoHistoryDto;
import com.example.cowmjucraft.domain.introduce.dto.common.IntroducePurposeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "소개 상세 정보 저장 요청 DTO")
public record AdminIntroduceDetailUpsertRequestDto(
        @Schema(description = "긴 소개 본문", nullable = true)
        IntroduceIntroDto intro,

        @Schema(description = "목적(Purpose)", nullable = true)
        IntroducePurposeDto purpose,

        @Schema(description = "현재 로고", nullable = true)
        IntroduceCurrentLogoDto currentLogo,

        @Size(max = 50, message = "logoHistories는 50개를 초과할 수 없습니다.")
        @Schema(description = "로고 히스토리 목록", nullable = true)
        List<IntroduceLogoHistoryDto> logoHistories
) {
}

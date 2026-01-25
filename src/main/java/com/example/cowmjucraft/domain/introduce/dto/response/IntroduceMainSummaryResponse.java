package com.example.cowmjucraft.domain.introduce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "소개 메인 요약 응답 DTO")
public record IntroduceMainSummaryResponse(

        @Schema(description = "제목", example = "명지공방 소개")
        String title,

        @Schema(description = "부제목", example = "우리의 방향과 비전")
        String subtitle,

        @Schema(description = "요약", example = "명지공방은 ...")
        String summary,

        @Schema(description = "메인 히어로 로고 S3 object key 목록")
        List<String> heroLogoKeys
) {
}

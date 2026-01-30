package com.example.cowmjucraft.domain.introduce.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소개 인트로(긴 소개 본문)")
public record IntroduceIntroDto(
        @Schema(description = "표시용 제목", example = "명지공방(明智工房)", nullable = true)
        String title,

        @Schema(description = "표시용 슬로건", example = "우리의 손끝에서, 명지가 피어납니다.", nullable = true)
        String slogan,

        @Schema(
                description = "긴 소개 본문(마크다운/줄바꿈 포함)",
                example = "안녕하세요!\\n명지공방은...",
                nullable = true
        )
        String body
) {
}

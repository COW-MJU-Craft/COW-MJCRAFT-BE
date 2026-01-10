package com.example.cowmjucraft.domain.introduce.intro.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "명지공방 소개 관리자 요청 DTO")
public record IntroduceAdminRequestDto(

        @NotBlank
        @Schema(
                description = "소개 제목",
                example = "명지공방 소개"
        )
        String title,

        @NotBlank
        @Schema(
                description = "소개 본문",
                example = "명지공방은 학생들이 함께 만드는 공방 서비스입니다."
        )
        String content
) {
}

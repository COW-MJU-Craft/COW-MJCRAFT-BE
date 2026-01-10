package com.example.cowmjucraft.domain.introduce.intro.dto.response;

import com.example.cowmjucraft.domain.introduce.intro.entity.IntroduceContent;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "명지공방 소개 응답 DTO")
public record IntroduceResponseDto(

        @Schema(
                description = "소개 제목",
                example = "명지공방 소개"
        )
        String title,

        @Schema(
                description = "소개 본문",
                example = "명지공방은 학생들이 함께 만드는 공방 서비스입니다."
        )
        String content
) {
    public static IntroduceResponseDto from(IntroduceContent content) {
        return new IntroduceResponseDto(
                content.getTitle(),
                content.getContent()
        );
    }
}

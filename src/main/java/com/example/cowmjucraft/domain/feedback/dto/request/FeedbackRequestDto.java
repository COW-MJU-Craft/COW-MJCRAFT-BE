package com.example.cowmjucraft.domain.feedback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "피드백 등록 요청 DTO")
public record FeedbackRequestDto(

        @NotBlank
        @Schema(
                description = "제목",
                example = "예약 화면 개선 요청"
        )
        String title,

        @NotBlank
        @Schema(
                description = "건의 내용",
                example = "예약 가능한 날짜를 달력으로 보여주면 좋겠습니다."
        )
        String content
) {
}

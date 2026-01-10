package com.example.cowmjucraft.domain.feedback.dto.response;

import com.example.cowmjucraft.domain.feedback.entity.Feedback;
import com.example.cowmjucraft.domain.feedback.entity.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드백 관리자 응답 DTO")
public record FeedbackAdminResponseDto(

        @Schema(
                description = "피드백 ID",
                example = "1"
        )
        Long id,

        @Schema(
                description = "제목",
                example = "예약 화면 개선 요청"
        )
        String title,

        @Schema(
                description = "건의 내용",
                example = "예약 가능한 날짜를 달력으로 보여주면 좋겠습니다."
        )
        String content,

        @Schema(
                description = "답변 내용",
                example = "좋은 의견 감사합니다. 다음 배포에 반영하겠습니다."
        )
        String answer,

        @Schema(
                description = "처리 상태",
                example = "ANSWERED"
        )
        FeedbackStatus status
) {
    public static FeedbackAdminResponseDto from(Feedback feedback) {
        return new FeedbackAdminResponseDto(
                feedback.getId(),
                feedback.getTitle(),
                feedback.getContent(),
                feedback.getAnswer(),
                feedback.getStatus()
        );
    }
}

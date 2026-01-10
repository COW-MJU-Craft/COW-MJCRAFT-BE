package com.example.cowmjucraft.domain.feedback.dto.request;

import com.example.cowmjucraft.domain.feedback.entity.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "피드백 관리자 답변/상태 변경 요청 DTO")
public record FeedbackAdminUpdateRequestDto(

        @Schema(
                description = "답변 내용",
                example = "좋은 의견 감사합니다. 다음 배포에 반영하겠습니다."
        )
        String answer,

        @NotNull
        @Schema(
                description = "처리 상태",
                example = "ANSWERED",
                allowableValues = {"RECEIVED", "ANSWERED"}
        )
        FeedbackStatus status
) {
}

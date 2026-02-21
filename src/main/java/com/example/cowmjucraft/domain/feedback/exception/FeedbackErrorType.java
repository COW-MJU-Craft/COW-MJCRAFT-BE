package com.example.cowmjucraft.domain.feedback.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackErrorType implements ErrorCode {

    FEEDBACK_NOT_FOUND(404, "피드백을 찾을 수 없습니다.");

    private final int httpStatusCode;
    private final String message;
}

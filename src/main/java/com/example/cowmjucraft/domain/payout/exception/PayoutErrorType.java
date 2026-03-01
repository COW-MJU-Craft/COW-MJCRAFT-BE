package com.example.cowmjucraft.domain.payout.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayoutErrorType implements ErrorCode {

    PAYOUT_NOT_FOUND(404, "정산서를 찾을 수 없습니다."),
    PAYOUT_ITEM_NOT_BELONG_TO_PAYOUT(400, "해당 정산서에 속하지 않는 항목입니다."),
    PAYOUT_ITEM_NOT_FOUND(404, "정산 항목을 찾을 수 없습니다."),

    PROJECT_NOT_FOUND(404, "해당 하는 프로젝트를 찾을 수 없습니다."),
    PAYOUT_ALREADY_EXISTS_FOR_PROJECT(409, "해당 프로젝트에 이미 정산서가 존재합니다."),
    PROJECT_NOT_CLOSED(403,"마감된 프로젝트만 정산서를 조회할 수 있습니다.");


    private final int httpStatusCode;
    private final String message;
}

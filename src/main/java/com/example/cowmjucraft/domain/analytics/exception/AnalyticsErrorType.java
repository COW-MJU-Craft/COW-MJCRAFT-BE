package com.example.cowmjucraft.domain.analytics.exception;

import com.example.cowmjucraft.global.response.type.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnalyticsErrorType implements ErrorCode {

    INVALID_DATE_RANGE(400, "조회 기간이 올바르지 않습니다. 시작일은 종료일보다 늦을 수 없고 최대 366일까지 조회할 수 있습니다."),
    NOT_CONFIGURED(503, "분석 연동이 설정되지 않았습니다."),
    UPSTREAM_FAILED(502, "분석 데이터를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");

    private final int httpStatusCode;
    private final String message;
}

package com.example.cowmjucraft.domain.analytics.service;

import java.time.LocalDate;
import java.util.List;

/** GA4 Data API의 runReport를 감싼 포트. 서비스 로직과 외부 호출을 분리해 테스트하기 쉽게 한다. */
public interface GaReportClient {

    List<Row> runReport(Query query);

    /**
     * @param eventNames 비어 있지 않으면 eventName 차원을 이 값들로 제한한다.
     */
    record Query(
            LocalDate from,
            LocalDate to,
            List<String> dimensions,
            List<String> metrics,
            List<String> eventNames
    ) {}

    record Row(List<String> dimensions, List<String> metrics) {}
}

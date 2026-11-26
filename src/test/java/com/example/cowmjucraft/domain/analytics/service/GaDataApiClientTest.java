package com.example.cowmjucraft.domain.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.cowmjucraft.domain.analytics.config.GaProperties;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsErrorType;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GaDataApiClientTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void GA_응답의_차원과_지표_값을_행으로_바꾼다() throws Exception {
        String json = """
                {"rows":[
                  {"dimensionValues":[{"value":"google"},{"value":"organic"}],"metricValues":[{"value":"38"},{"value":"12"}]},
                  {"dimensionValues":[{"value":"(direct)"},{"value":"(none)"}],"metricValues":[{"value":"145"},{"value":"102"}]}
                ],"rowCount":2}
                """;

        List<GaReportClient.Row> rows = GaDataApiClient.parseRows(MAPPER, json);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).dimensions()).containsExactly("google", "organic");
        assertThat(rows.get(0).metrics()).containsExactly("38", "12");
        assertThat(rows.get(1).dimensions()).containsExactly("(direct)", "(none)");
    }

    @Test
    void 데이터가_없으면_rows_필드가_없어도_빈_목록이다() throws Exception {
        assertThat(GaDataApiClient.parseRows(MAPPER, "{\"rowCount\":0}")).isEmpty();
    }

    @Test
    void 이벤트_이름이_있으면_차원_필터를_요청에_넣는다() {
        GaReportClient.Query query = new GaReportClient.Query(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 26),
                List.of("sessionSource", "eventName"), List.of("totalUsers"), List.of("order_submit"));

        Map<String, Object> request = GaDataApiClient.buildRequest(query);

        assertThat(request).containsKey("dimensionFilter");
        assertThat(request.get("limit")).isEqualTo(10_000);
        assertThat(request.get("dateRanges")).isEqualTo(
                List.of(Map.of("startDate", "2026-11-01", "endDate", "2026-11-26")));
    }

    @Test
    void 이벤트_이름이_없으면_차원_필터를_넣지_않는다() {
        GaReportClient.Query query = new GaReportClient.Query(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 26),
                List.of("date"), List.of("sessions"), List.of());

        assertThat(GaDataApiClient.buildRequest(query)).doesNotContainKey("dimensionFilter");
    }

    @Test
    void 설정이_없으면_외부_호출_없이_503용_예외를_던진다() {
        GaDataApiClient client = new GaDataApiClient(new GaProperties(), MAPPER);
        GaReportClient.Query query = new GaReportClient.Query(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 26), List.of("date"), List.of("sessions"), List.of());

        assertThatThrownBy(() -> client.runReport(query))
                .isInstanceOfSatisfying(AnalyticsException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(AnalyticsErrorType.NOT_CONFIGURED));
    }
}

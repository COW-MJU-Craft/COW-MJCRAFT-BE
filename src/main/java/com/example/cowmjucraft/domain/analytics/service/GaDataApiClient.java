package com.example.cowmjucraft.domain.analytics.service;

import com.example.cowmjucraft.domain.analytics.config.GaProperties;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsErrorType;
import com.example.cowmjucraft.domain.analytics.exception.AnalyticsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * GA4 Data API(v1beta) REST 호출. gRPC 클라이언트 대신 REST를 써서 의존성을 작게 유지한다.
 * 서비스 계정 토큰은 google-auth-library가 만료 전까지 캐시한다.
 */
@Slf4j
@Component
public class GaDataApiClient implements GaReportClient {

    private static final String SCOPE = "https://www.googleapis.com/auth/analytics.readonly";
    private static final String ENDPOINT = "https://analyticsdata.googleapis.com/v1beta/properties/%s:runReport";
    private static final int ROW_LIMIT = 10_000;

    private final GaProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    private GoogleCredentials credentials;

    public GaDataApiClient(GaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(15).toMillis());
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public List<Row> runReport(Query query) {
        if (!properties.isConfigured()) {
            throw new AnalyticsException(AnalyticsErrorType.NOT_CONFIGURED);
        }
        try {
            String token = accessToken();
            String body = restClient.post()
                    .uri(ENDPOINT.formatted(properties.getPropertyId().trim()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(token))
                    .body(objectMapper.writeValueAsString(buildRequest(query)))
                    .retrieve()
                    .body(String.class);
            return parseRows(objectMapper, body);
        } catch (AnalyticsException e) {
            throw e;
        } catch (Exception e) {
            // 응답 본문이나 토큰이 로그에 남지 않도록 예외 종류만 기록한다.
            log.warn("GA Data API 호출 실패: {}", e.getClass().getSimpleName());
            throw new AnalyticsException(AnalyticsErrorType.UPSTREAM_FAILED);
        }
    }

    static Map<String, Object> buildRequest(Query query) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("dateRanges", List.of(Map.of(
                "startDate", query.from().toString(),
                "endDate", query.to().toString())));
        request.put("dimensions", query.dimensions().stream().map(name -> Map.of("name", name)).toList());
        request.put("metrics", query.metrics().stream().map(name -> Map.of("name", name)).toList());
        if (!query.eventNames().isEmpty()) {
            request.put("dimensionFilter", Map.of("filter", Map.of(
                    "fieldName", "eventName",
                    "inListFilter", Map.of("values", query.eventNames()))));
        }
        request.put("limit", ROW_LIMIT);
        return request;
    }

    static List<Row> parseRows(ObjectMapper objectMapper, String json) throws IOException {
        JsonNode rows = objectMapper.readTree(json).path("rows");
        List<Row> result = new ArrayList<>();
        for (JsonNode row : rows) {
            result.add(new Row(values(row.path("dimensionValues")), values(row.path("metricValues"))));
        }
        return result;
    }

    private static List<String> values(JsonNode nodes) {
        List<String> values = new ArrayList<>();
        for (JsonNode node : nodes) {
            values.add(node.path("value").asText(""));
        }
        return values;
    }

    private synchronized String accessToken() throws IOException {
        if (credentials == null) {
            byte[] key = Base64.getDecoder().decode(properties.getCredentialsBase64().trim());
            credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(key)).createScoped(List.of(SCOPE));
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
